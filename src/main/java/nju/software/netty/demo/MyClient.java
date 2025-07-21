package nju.software.netty.demo;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.codec.string.LineEncoder;
import io.netty.handler.codec.string.LineSeparator;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.StringUtil;

public class MyClient {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup eventExecutors = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventExecutors)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
//                            ch.pipeline().addLast(new LineEncoder(LineSeparator.DEFAULT, CharsetUtil.UTF_8));
//                            ch.pipeline().addLast(new MyClientHandler());
                            ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                            ch.pipeline().addLast(new ProtobufEncoder());
                            ch.pipeline().addLast(new GoogleClientHandler());

                        }
                    });
            System.out.println("Netty客户端准备完毕，可以随时连接服务端...");

//            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 6666).sync(); // .sync表示阻塞直到连接完成
            /**
             * 普通写法下，bootstrap.connect是异步的，ChannelFuture采用类似观察者模式的方式获得连接结果
             */
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 6666);
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        System.out.println("连接成功");
                    } else {
                        System.out.println("连接失败");
                    }
                }
            });


            channelFuture.channel().closeFuture().sync();
        } finally {
            eventExecutors.shutdownGracefully();
        }
    }

    /**
     * 测试Google Protocol Buf
     */
    private static class GoogleClientHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            for (int i = 0; i < 100; i++) {
                MessageProto.Message message = MessageProto
                        .Message
                        .newBuilder()
                        .setId(i)
                        .setContent("小" + i + "说Google是世界上最伟大的公司")
                        .build();
                ctx.writeAndFlush(message);
            }
        }
    }

    /**
     * 测试tcp粘包拆包
     */
    private static class TCPClientHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            for (int i = 0; i < 5; i++) {
//                ByteBuf byteBuf = Unpooled.copiedBuffer("msg_No" + i + StringUtil.LINE_FEED + "这是分隔符后面的字符串，不要这样用！", CharsetUtil.UTF_8);
//                ByteBuf byteBuf = Unpooled.copiedBuffer("msg_\nNo" + i + StringUtil.LINE_FEED, CharsetUtil.UTF_8);

                // 当接收端使用LengthFieldBasedDecoder
                byte[] bytes = ("message No" + i).getBytes(CharsetUtil.UTF_8);
                ByteBuf byteBuf = Unpooled.buffer(1024);

                byteBuf.writeInt(bytes.length);
                byteBuf.writeBytes(bytes);

                ctx.channel().writeAndFlush(byteBuf);
            }
        }
    }


    private static class MyClientHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(Unpooled.copiedBuffer("连接已经建立！！！", CharsetUtil.UTF_8));
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf byteBuf = (ByteBuf) msg;
            System.out.println("收到服务器" + ctx.channel().remoteAddress() + "消息：" + byteBuf.toString(CharsetUtil.UTF_8));
        }
    }
}
