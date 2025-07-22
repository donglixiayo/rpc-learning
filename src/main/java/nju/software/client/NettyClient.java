package nju.software.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import nju.software.client.model.RpcRequest;
import nju.software.common.NettyKryoDecoder;
import nju.software.common.NettyKryoEncoder;
import nju.software.serialize.kryo.KryoSerializer;
import nju.software.server.model.RpcResponse;

/**
 * @author Han
 * @Description
 */
@Slf4j
public class NettyClient {
    private static Bootstrap bootstrap;
    private String ip;
    private int port;

    public NettyClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    static {
        EventLoopGroup executors = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(executors)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast();
                        ch.pipeline().addLast(new NettyKryoDecoder(new KryoSerializer(), RpcResponse.class));
                        ch.pipeline().addLast(new NettyKryoEncoder(new KryoSerializer(), RpcRequest.class));
                    }
                });
    }

    public RpcResponse sendMsg(RpcRequest rpcRequest) {
        try {
            ChannelFuture f = bootstrap.connect(ip, port).sync();
            // 异步加回调
            // netty保证回调一定执行
            bootstrap.connect(ip, port).addListener((ChannelFuture future) -> {
                if (future.isSuccess()) {
                    log.info("连接成功");

                    Channel channel = future.channel();
                    // 发送消息
                    channel.writeAndFlush(rpcRequest).addListener( writeFuture -> {
                        if (writeFuture.isSuccess()) {
                            log.info("...");
                        } else {
                            log.error("...");
                        }
                    });

                    // 监听关闭
                    channel.closeFuture().addListener((ChannelFuture closeFuture) -> {
                        AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
                        RpcResponse rpcResponse = closeFuture.channel().attr(key).get();
                        if (rpcResponse == null) {
                            // rpcResponse 有可能为空，即通道关闭也没有收到任何来自Server的消息
                        }
                    });
                } else {
                    log.error("连接失败");
                }
            });


            Channel futureChannel = f.channel();
            if (futureChannel != null) {
                futureChannel.writeAndFlush(rpcRequest).addListener(future -> {
                    if (future.isSuccess()) {
                        log.info("Client send msg successfully: {}", rpcRequest);
                    } else {
                        log.error("Client send msg failed, cause: ", future.cause());
                    }
                });
                futureChannel.closeFuture().sync();
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
                return futureChannel.attr(key).get();
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}
