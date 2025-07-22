package nju.software.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import nju.software.serialize.Serializer;


@AllArgsConstructor
public class NettyKryoEncoder extends MessageToByteEncoder<Object> {
    private final Serializer serializer;
    private final Class<?> clazz;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if (clazz.isInstance(o)) {
            byte[] bytes = serializer.serialize(o);
            int length = bytes.length;
            byteBuf.writeInt(length); // 将长度写入Netty消息
            byteBuf.writeBytes(bytes);
        }
    }
}
