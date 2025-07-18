package nju.software.netty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;

public class GroupChatClient {
    private Selector selector;
    private SocketChannel socketChannel;

    public GroupChatClient() throws IOException {
        this.selector = Selector.open();
        this.socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", GroupChatServer.PORT));
        this.socketChannel.configureBlocking(false);
        this.socketChannel.register(this.selector, SelectionKey.OP_READ);
        System.out.println("客户端启动~");
    }

    public void sendMessage(String message) throws IOException {
        socketChannel.write(ByteBuffer.wrap(message.getBytes()));
    }

    public void readMessage() throws IOException {
        if (selector.select() > 0) {
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey selectionKey = it.next();
                if (selectionKey.isReadable()) {
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    channel.read(buffer);
                    System.out.println(new String(buffer.array(), 0, buffer.position()));
                }
                it.remove();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        GroupChatClient client = new GroupChatClient();
        new Thread(() -> {
            while (true) {
                try {
                    client.readMessage();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String content = scanner.nextLine();
            client.sendMessage(content);
        }
    }
}
