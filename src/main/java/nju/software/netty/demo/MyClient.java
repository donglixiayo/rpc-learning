package nju.software.netty.demo;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

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
                            ch.pipeline().addLast(new MyClientHandler());
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
