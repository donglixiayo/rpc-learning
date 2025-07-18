package nju.software.netty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class GroupChatServer {
    public static final int PORT = 8888;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    public GroupChatServer() throws IOException {
        selector = Selector.open();

        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress("127.0.0.1", PORT));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void listen() throws IOException {
        while (true) {
            if (selector.select(2000) > 0) {
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey selectedKey = it.next();
                    if (selectedKey.isAcceptable()) {
                        SocketChannel channel = serverSocketChannel.accept();
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_READ);
                        System.out.println(channel.getRemoteAddress() + "上线了~");
                    }
                    if (selectedKey.isReadable()) {
                        readAndDistributeData(selectedKey);
                    }
                }

                it.remove();
            }
        }
    }

    private void readAndDistributeData(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        channel.read(buffer);
        distribute(channel.getRemoteAddress() + "说：" + new String(buffer.array(), 0, buffer.position()), channel);
    }

    private void distribute(String msg, SocketChannel channel) throws IOException {
        for (SelectionKey key : selector.keys()) {
            SelectableChannel selectableChannel = key.channel();
            if (selectableChannel instanceof SocketChannel objChannel && selectableChannel != channel) {
                objChannel.write(ByteBuffer.wrap(msg.getBytes()));
            }
        }
    }

    public static void main(String[] args) throws IOException {
        GroupChatServer groupChatServer = new GroupChatServer();
        groupChatServer.listen();
    }
}
