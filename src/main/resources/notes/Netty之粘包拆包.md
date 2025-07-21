# Netty之粘包拆包

## 1 问题描述

Netty的Bytebuf底层是基于TCP的，我们知道TCP是基于字节流的传输层协议，不规定消息的边界。

<img src="C:\Users\Belieber\AppData\Roaming\Typora\typora-user-images\image-20250719160350708.png" alt="image-20250719160350708" style="zoom:67%;" />

所以Netty进行默认的消息发送行为可能导致拆包和粘包。

客户端示例代码：

```java
 for (int i = 1; i <= 5; i++) {
    ByteBuf byteBuf = Unpooled.copiedBuffer("msg No" + i + " ", Charset.forName("utf-8"));
    ctx.writeAndFlush(byteBuf);
}
```

服务端代码：

```java
//count变量，用于计数
private int count = 0;

@Override
protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
    byte[] bytes = new byte[msg.readableBytes()];
    //把ByteBuf的数据读到bytes数组中
    msg.readBytes(bytes);
    String message = new String(bytes, Charset.forName("utf-8"));
    System.out.println("服务器接收到数据：" + message);
    //打印接收的次数
    System.out.println("接收到的数据量是：" + (++this.count));
}
```

启动后的效果：

![image-20250719160521530](C:\Users\Belieber\AppData\Roaming\Typora\typora-user-images\image-20250719160521530.png)

## 2 解决方案

整体有三个思路：

- ==特殊符号==：在数据的末尾添加特殊的符号标识数据包的边界。通常会加\n\r、\t或者其他的符号。
- ==自定义长度==：在数据的头部声明数据的长度，按长度获取数据。
- ==固定长度==:规定报文的长度，不足则补空位。读取时按规定好的长度来读取。

### 2.1 LineBaseFrameDecodeer

LineBasedFrameDecoder是Netty内置的解码器，对应的编码器为LineEncoder。

原理是第一种思路，默认加上换行符。

发送方示例代码：

```java
@Override
protected void initChannel(SocketChannel ch) throws Exception {
    //添加编码器，使用默认的符号\n，字符集是UTF-8
    ch.pipeline().addLast(new LineEncoder(LineSeparator.DEFAULT, CharsetUtil.UTF_8));
    ch.pipeline().addLast(new TcpClientHandler());
}
```

接收方示例代码：

```java
@Override
protected void initChannel(SocketChannel ch) throws Exception {
    //解码器需要设置数据的最大长度，这里设置成1024
    ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
    //给pipeline管道设置业务处理器
    ch.pipeline().addLast(new TcpServerHandler());
}
```

最后，在发送方发送消息的时候，**手动在消息末尾加上标识符**：

```java
@Override
public void channelActive(ChannelHandlerContext ctx) throws Exception {
    for (int i = 1; i <= 5; i++) {
        //在末尾加上默认的标识符\n
        ByteBuf byteBuf = Unpooled.copiedBuffer("msg No" + i + StringUtil.LINE_FEED, Charset.forName("utf-8"));
        ctx.writeAndFlush(byteBuf);
    }
}
```

注意有个问题：如果消息中本身就含有\n分隔符，就会发生误拆包！

### 2.2 自定义长度帧解码器

LengthFieldBasedFrameDecoder。采用原理二解决粘包，即在数据的头部声明数据长度。

该解码器需要定义5个参数：

<img src="C:\Users\Belieber\AppData\Roaming\Typora\typora-user-images\image-20250719162349862.png" alt="image-20250719162349862" style="zoom:60%;" />

+ ==maxFrameLength==  发送数据包的最大长度
+ ==lengthFieldOffset==长度域的偏移量。长度域位于整个数据包字节数组中的开始下标。
+ ==lengthFieldLength==  长度域的字节数长度。长度域的字节数长度。
+ ==lengthAdjustment==  长度域的偏移量矫正。如果长度域的值，除了包含有效数据域的长度外，还包含了其他域（如长度域自身）长度，那么，就需要进行矫正。矫正的值为：**包长 - 长度域的值 – 长度域偏移 – 长度域长**。
+ ==initialBytesToStrip==  丢弃的起始字节数。丢弃处于此索引值前面的字节。只要后面的数据。一般都是==丢弃长度域的数据==。当然如果你希望得到全部数据，那就设置为0。

接收端示例代码：

```java
@Override
protected void initChannel(SocketChannel ch) throws Exception {
    //数据包最大长度是1024
    //长度域的起始索引是0
    //长度域的数据长度是4
    //矫正值为0，因为长度域只有 有效数据的长度的值
    //丢弃数据起始值是4，因为长度域长度为4，我要把长度域丢弃，才能得到有效数据
    ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4));
    ch.pipeline().addLast(new TcpClientHandler());
}
```

Client端发送消息配置：

```java
@Override
public void channelActive(ChannelHandlerContext ctx) throws Exception {
    for (int i = 1; i <= 5; i++) {
        String str = "msg No" + i;
        ByteBuf byteBuf = Unpooled.buffer(1024);
        byte[] bytes = str.getBytes(Charset.forName("utf-8"));
        //设置长度域的值，为有效数据的长度
        byteBuf.writeInt(bytes.length);
        //设置有效数据
        byteBuf.writeBytes(bytes);
        ctx.writeAndFlush(byteBuf);
    }
}
```

### 2.3 使用Google Protobuf编解码器

> Protocol buffers是Google公司的**与语言无关、平台无关、可扩展的序列化数据的机制**，类似XML，但是**更小、更快、更简单**。您只需**定义一次数据的结构化方式**，然后就可以使用**特殊生成的源代码**，轻松地**将结构化数据写入和读取到各种数据流中，并支持多种语言**。

首先下载protoc（https://github.com/protocolbuffers/protobuf/releases），编写.proto文件：

```protobuf
syntax = "proto3"; //版本
option java_outer_classname = "MessagePojo";//生成的外部类名，同时也是文件名

message Message {
    int32 id = 1;//Message类的一个属性，属性名称是id，序号为1
    string content = 2;//Message类的一个属性，属性名称是content，序号为2
}
```

使用命令生成类：

```shell
protoc --java_out=. Message.proto
```

添加Maven依赖：

```xml
<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
    <version>3.6.1</version>
</dependency>
```

客户端添加编码器：

```Java
@Override
protected void initChannel(SocketChannel ch) throws Exception {
    //在发送端添加Protobuf编码器
    ch.pipeline().addLast(new ProtobufEncoder());
    ch.pipeline().addLast(new TcpClientHandler());
}
```

服务端添加解码器：

```java
@Override
protected void initChannel(SocketChannel ch) throws Exception {
    //添加Protobuf解码器，构造器需要指定解码具体的对象实例
    ch.pipeline().addLast(new ProtobufDecoder(MessagePojo.Message.getDefaultInstance()));
    //给pipeline管道设置处理器
    ch.pipeline().addLast(new TcpServerHandler());
}
```

客户端发送代码：

```java
private static class GoogleClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        MessageProto.Message message = MessageProto
                .Message
                .newBuilder()
                .setId(1)
                .setContent("Google是世界上最伟大的公司")
                .build();
        ctx.writeAndFlush(message);
    }
}
```

然而单纯使用ProtobufEncoder和ProtobufDecoder是不能解决粘包拆包问题的，可以使用以下代码实验：

```java
for (int i = 0; i < 100; i++) {
    MessageProto.Message message = MessageProto
            .Message
            .newBuilder()
            .setId(i)
            .setContent("小" + i + "说Google是世界上最伟大的公司")
            .build();
    ctx.writeAndFlush(message);
}
```

而且Server端会直接报错，Client没有任何警告！

要解决就使用编码器==ProtobufVarint32LengthFieldPrepender==和解码器==ProtobufVaint32FrameDecoder==。

```java
ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender()); // Client
ch.pipeline().addLast(new ProtobufVarint32FrameDecoder()); // Server
```

简单看一下decode源码：

```java
@Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
        throws Exception {
    in.markReaderIndex(); // 标记当前位置，后续读取不成功可以回退
    int preIndex = in.readerIndex(); // 当前读取位置
    int length = readRawVarint32(in); // 读取Varint格式的长度变量为length
    if (preIndex == in.readerIndex()) { // 位置不变，说明length读取失败，即当前流in的剩余长度不够一个Varint变量
        return;
    }
    if (length < 0) {
        throw new CorruptedFrameException("negative length: " + length);
    }

    if (in.readableBytes() < length) {
        in.resetReaderIndex(); // 剩余消息长度不够length，则继续
    } else {
        out.add(in.readRetainedSlice(length));
    }
}
```

> Varint即Variable-length integer，变长整数，默认无符号。
>
> 核心思想是用尽量少的字节表示较小的数字。
>
> 编码规则：将一个整数拆成7位一组，每一组放到一个字节中，字节中剩余的高一位用`1`表示后面还有更多字节，用`0`表示这是最后一个字节。
>
> ![image-20250721225020361](assets/image-20250721225020361.png)



