package nju.software.netty.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.Array;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Han
 * @Description TODO
 */
@Slf4j
public class NIOTest {
    public static void main(String[] args) {
        // buffer代码
//        String str = "这就是测试字符串。";
//        ByteBuffer buffer = ByteBuffer.allocate(1024);
//        byte[] strBytes = str.getBytes();
//        buffer.put(strBytes);
//        byte[] temp = new byte[strBytes.length];
//        buffer.flip();
//        int pos = 0;
//        while (buffer.hasRemaining()) {
//            temp[pos] = buffer.get();
//            pos++;
//        }
//        System.out.println(new String(temp));

        // file channel code
//        File inFile = new File("aa.txt");
//
//        try (FileInputStream fis = new FileInputStream(inFile);
//             FileOutputStream fos = new FileOutputStream(new File("bb.txt"))) {
//            FileChannel inChannel = fis.getChannel();
//            FileChannel outChannel = fos.getChannel();
//            ByteBuffer buffer = ByteBuffer.allocate((int) inFile.length());
//            inChannel.read(buffer);
//            buffer.flip();
//            int size = outChannel.write(buffer);
//            System.out.println("write " + size + " bytes");
//
//        } catch (IOException e) {
//            System.out.println(e.getMessage());
//        }

        // 阻塞式SocketChannel
//        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
//            InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 8080);
//            serverSocketChannel.bind(inetSocketAddress);
//            ByteBuffer buffer = ByteBuffer.allocate(1024);
//            while (true) {
//                SocketChannel socketChannel = serverSocketChannel.accept();
//                int length;
//                while ((length = socketChannel.read(buffer))!= -1) {
//                    System.out.println(new String(buffer.array(), 0, length));
//                    buffer.clear();
//                }
//            }
//
//        } catch (IOException e) {
//            System.out.println(e.getMessage());
//        }

        // transferTo & transferFrom
//        File inFile = new File("aa.txt");
//        try (FileInputStream fis = new FileInputStream(inFile);
//             FileOutputStream fos = new FileOutputStream(new File("bb.txt"))) {
//            FileChannel fisChannel = fis.getChannel();
//            FileChannel fosChannel = fos.getChannel();
//
////            ByteBuffer buffer = ByteBuffer.allocate((int) inFile.length());
////            System.out.println("ByteBuffer.limit = " + buffer.limit());
//            System.out.println("InFile.len = " + inFile.length());
////            fisChannel.transferTo(0, inFile.length(), fosChannel);
//            long len = fosChannel.transferFrom(fisChannel, 0, inFile.length());
//            System.out.println("Transfer byte: " + len);
//
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        // 分散读取，聚合写入
//        File inFile = new File("aa.txt");
//        try (FileInputStream fis = new FileInputStream(inFile);
//             FileOutputStream fos = new FileOutputStream(new File("bb.txt"))) {
//            FileChannel inChannel = fis.getChannel();
//            FileChannel outChannel = fos.getChannel();
//
//            ByteBuffer buffer1 = ByteBuffer.allocate(5);
//            ByteBuffer buffer2 = ByteBuffer.allocate(5);
//            ByteBuffer buffer3 = ByteBuffer.allocate(5);
//            ByteBuffer[] buffers = new ByteBuffer[] {buffer1, buffer2, buffer3};
//
//            long sumLen = 0, len = 0;
//            while ((len = inChannel.read(buffers)) != -1) {
//                sumLen += len;
//
//                Arrays.stream(buffers)
//                        .map(buffer -> "position=" + buffer.position() + ", limit=" +buffer.limit() + ", capacity=" + buffer.capacity())
//                        .forEach(System.out::println);
//
//                Arrays.stream(buffers).forEach(Buffer::flip);
//
//                long write = outChannel.write(buffers);
//                System.out.println("write=" + write);
//
//                Arrays.stream(buffers).forEach(Buffer::clear);
//            }
//
//            System.out.println("sumLen=" + sumLen);
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

          // ByteBufferffer vs DirectByteBuffer
//        long start = System.currentTimeMillis();
//        File file = new File("E:\\video\\shameless\\S11E02.mp4");
//        try (FileInputStream fis = new FileInputStream(file);
//            FileOutputStream fos = new FileOutputStream("copy.mp4")) {
//            FileChannel inChannel = fis.getChannel();
//            FileChannel outChannel = fos.getChannel();
//
////            ByteBuffer buffer = ByteBuffer.allocate(50 * 1024 * 1024); // Cost 4165ms
//            ByteBuffer buffer = ByteBuffer.allocateDirect(50 * 1024 * 1024); // Cost 2631ms
//            while (inChannel.read(buffer) != -1) {
//                buffer.flip();
//                outChannel.write(buffer);
//                buffer.clear();
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        long end = System.currentTimeMillis();
//        System.out.println("Cost " + (end - start) + "ms");

    }
}
