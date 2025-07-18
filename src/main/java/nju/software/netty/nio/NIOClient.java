package nju.software.netty.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@Slf4j
public class NIOClient {
    public static void main(String[] args) {
        try (SocketChannel socketChannel = SocketChannel.open()) {
            InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 8080);
            socketChannel.configureBlocking(false);
            boolean connect = socketChannel.connect(inetSocketAddress);
            if (!connect) {
                while (!socketChannel.finishConnect()) {
                    log.info("连接中，期间可以做其他事情");
                    log.info("做其他事情...");
                    log.info("做其他事情...");
                    log.info("做其他事情...");
                }
            }
            String msg = "Hello NIO Server!";
            ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
            socketChannel.write(buffer);
            int read = System.in.read(); // 让程序卡在这个位置
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
