package nju.software.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author Han
 * @Description TODO
 */
public class HelloClient {
    Logger logger = LoggerFactory.getLogger(HelloClient.class);

    public Object send(Message message, String host, int port) {
        // 1.创建Socket对象并指定服务器地址和端口号
        try (Socket socket = new Socket(host, port)) {
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())) {
                // 2.通过输出流向服务器发送请求信息
                objectOutputStream.writeObject(message);
                // 3.通过输入流读取服务器响应
                return objectInputStream.readObject();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        HelloClient helloClient = new HelloClient();
        Message msg = new Message("Hello World!");
        String host = "127.0.0.1";
        int port = 6666;
        Object response = helloClient.send(msg, host, port);
        System.out.println(((Message) response).getContent());
    }
}
