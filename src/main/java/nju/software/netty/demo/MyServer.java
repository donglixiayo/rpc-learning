package nju.software.netty.demo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.util.CharsetUtil;

public class MyServer {
    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
//                            socketChannel.pipeline().addLast(new MyServerHandler());
//                            socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024));
                            // 使用LengthFieldBasedFrameDecoder解决粘包问题：
//                            socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4));
                            socketChannel.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                            socketChannel.pipeline().addLast(new ProtobufDecoder(MessageProto.Message.getDefaultInstance()));
                            socketChannel.pipeline().addLast(new GoogleServerHandler());
                        }
                    });
            System.out.println("Netty服务端准备就绪...");
            ChannelFuture channelFuture = bootstrap.bind(6666).sync();
            channelFuture.channel().closeFuture().sync();

        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    /**
     * 测试Google protocol
     */
    private static class GoogleServerHandler extends SimpleChannelInboundHandler<MessageProto.Message> {
        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, MessageProto.Message message) throws Exception {
            System.out.println("id: " + message.getId());
            System.out.println("content: " + message.getContent());
        }
    }

    /**
     * 测试TCP粘包拆包
     */
    private static class TCPServerHandler extends SimpleChannelInboundHandler<ByteBuf> {
        private int count = 0;

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(bytes);
            System.out.println("服务端接收数据: " + new String(bytes, CharsetUtil.UTF_8));
            System.out.println("服务端接收数据次数: " + (++count));
        }
    }

    private static class MyServerHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {


//            ctx.channel().eventLoop().execute(() -> {
//                // 使用netty workerGroup线程池处理耗时任务
//                // 更好的实践是创建单独的业务线程池
//            });
//            ctx.channel().eventLoop().schedule(() -> {}, 3000, TimeUnit.MILLISECONDS);


            // netty默认的msg类型是ByteBuf
            ByteBuf byteBuf = (ByteBuf) msg;
            System.out.println("Receive msg from " + ctx.channel().remoteAddress() + ": " + byteBuf.toString(CharsetUtil.UTF_8));
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            // channelReadComplete往往在接收到完整的TCP包后进行回调
            ctx.writeAndFlush(Unpooled.copiedBuffer("服务端收到消息，并向你扣了一个问号？", CharsetUtil.UTF_8));
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            // 处理过程中发生异常的回调逻辑
            // 关闭通道
            ctx.close();
        }
    }
}
