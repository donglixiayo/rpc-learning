package nju.software.netty.nio;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * @author Han
 * @Description TODO
 */
public class NIOTest {
    public static void main(String[] args) {
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

        // ByteBuffer vs DirectByteBuffer
        long start = System.currentTimeMillis();
        File file = new File("E:\\video\\shameless\\S11E02.mp4");
        try (FileInputStream fis = new FileInputStream(file);
            FileOutputStream fos = new FileOutputStream("copy.mp4")) {
            FileChannel inChannel = fis.getChannel();
            FileChannel outChannel = fos.getChannel();

//            ByteBuffer buffer = ByteBuffer.allocate(50 * 1024 * 1024); // Cost 4165ms
            ByteBuffer buffer = ByteBuffer.allocateDirect(50 * 1024 * 1024); // Cost 2631ms
            while (inChannel.read(buffer) != -1) {
                buffer.flip();
                outChannel.write(buffer);
                buffer.clear();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        long end = System.currentTimeMillis();
        System.out.println("Cost " + (end - start) + "ms");
    }
}
