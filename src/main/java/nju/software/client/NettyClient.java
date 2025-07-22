package nju.software.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import nju.software.serialize.kryo.KryoSerializer;
import nju.software.server.model.RpcResponse;

/**
 * @author Han
 * @Description
 */
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
                    }
                });
    }

    public void sendMsg() {

    }
}
