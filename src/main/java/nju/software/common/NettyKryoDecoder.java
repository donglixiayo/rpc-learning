package nju.software.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import nju.software.serialize.Serializer;

import java.nio.ByteBuffer;
import java.util.List;

@Slf4j
public class NettyKryoDecoder extends ByteToMessageDecoder {
    private final Serializer serializer;
    private final Class<?> clazz;
    private static final int BODY_LENGTH_PART = 4; // 消息长度，我们在编码器编码的时候将一个int类型的长度写了进去

    public NettyKryoDecoder(Serializer serializer, Class<?> clazz) {
        this.serializer = serializer;
        this.clazz = clazz;
    }

    /**
     *
     * @param channelHandlerContext
     * @param in
     * @param out 解码后的数据对象要添加到out中
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        // 1.我们往消息中写入的消息长度是int类型，占4个字节
        //   所以消息必须比4字节长才合法
        if (in.readableBytes() >= BODY_LENGTH_PART) {
            // 2.标记当前readIndex位置，以便后面进行重置
            in.markReaderIndex();
            // 3.读取我们自己写入的消息长度
            int length = in.readInt();
            // 4.消息长度不合法 or 消息剩余长度不合法
            if (length < 0 || in.readableBytes() < 0) {
                log.error("Invalid data length or readable bytes length!");
                return;
            }
            // 5.若可读消息小于消息长度，则重置readIndex，返回
            if (in.readableBytes() < length) {
                in.resetReaderIndex();
                return;
            }
            // 6.读取消息
            ByteBuffer buffer = ByteBuffer.allocate(length);
            ByteBuf byteBuf = in.readBytes(buffer);
            Object obj = serializer.deserialize(buffer.array(), clazz);
            out.add(obj);
            log.info("Succeed to decode object: {}", obj);
        }
    }
}
