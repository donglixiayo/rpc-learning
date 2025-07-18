package nju.software.netty.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

@Slf4j
public class NIOServer {
    public static void main(String[] args) {
        /**
         * Selector实现同步非阻塞NIO
         */
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
             Selector selector = Selector.open()) {
            InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 8080);
            serverSocketChannel.bind(inetSocketAddress);
            serverSocketChannel.configureBlocking(false);

            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                if (selector.select(3000) == 0) {
                    log.info("服务器等待3秒，没有连接");
                    continue;
                }
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectionKeys.iterator();
                while (it.hasNext()) {
                    SelectionKey selectionKey = it.next();
                    if (selectionKey.isAcceptable()) {
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        log.info("连接成功: {}", socketChannel.getRemoteAddress());
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    }
                    if (selectionKey.isReadable()) {
                        SocketChannel channel = (SocketChannel) selectionKey.channel();
                        ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();

                        channel.read(buffer);
                        log.info("Receive data: {}", new String(buffer.array(), 0, buffer.limit()));

                        channel.close();
                    }
                    it.remove();
                }

            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
