package nju.software.serialize;

/**
 * @author Han
 * @Description TODO
 */
public interface Serializer {
    byte[] serialize(Object obj);

    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
