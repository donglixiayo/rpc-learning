package nju.software.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Han
 * @Description TODO
 */
public class HelloServer {
    private static final Logger logger = LoggerFactory.getLogger(HelloServer.class);

    public void start(int port) {
        // 1.创建ServerSocket对象并绑定一个端口
        try (ServerSocket server = new ServerSocket(port)) {
            // 2.通过accept()方法监听客户端请求
            Socket socket;
            while ((socket = server.accept()) != null) {
                logger.info("client connected: {}", socket.getPort());
                try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                     ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
                    // 3.通过输入流读取客户端发送的请求信息
                    Message message = (Message) objectInputStream.readObject();
                    logger.info("receive message: {}", message.getContent());
                    // 4.通过输出流向客户端发送响应信息
                    Message reply = new Message("reply...");
                    objectOutputStream.writeObject(reply);
                    objectOutputStream.flush(); // 记得flush
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        HelloServer helloServer = new HelloServer();
        helloServer.start(6666);

    }
}
