package nju.software.serialize.kryo;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import nju.software.serialize.Serializer;
import nju.software.socket.Message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author Han
 * @Description
 */
public class KryoSerializer implements Serializer {
    public static void main(String[] args) {
        KryoSerializer kryoSerializer = new KryoSerializer();
        Message message = new Message("this is a test message!");
        byte[] bytes = kryoSerializer.serialize(message);
        Message msg = kryoSerializer.deserialize(bytes, Message.class);
        System.out.println(msg.getContent());
    }

    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        Output output = new Output(new ByteArrayOutputStream());
        Kryo kryo = kryoThreadLocal.get();
        kryo.writeObject(output, obj);
        kryoThreadLocal.remove();

        return output.toBytes();
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        Input input = new Input(new ByteArrayInputStream(bytes));
        Kryo kryo = kryoThreadLocal.get();
        T t = kryo.readObject(input, clazz);
        kryoThreadLocal.remove();

        return t;
    }
}
