package com.mwl.ch09;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * ---------------------
 *|   4    |  4  |  ?   |
 * ---------------------
 *| length | age | name |
 * ---------------------
 */
/**
 * @author mwl
 * @date 2019/01/28
 */
public class Encoder extends MessageToByteEncoder<User> {
    @Override
    protected void encode(ChannelHandlerContext ctx, User user, ByteBuf out) throws Exception {

        byte[] bytes = user.getName().getBytes();
        out.writeInt(4 + bytes.length);
        out.writeInt(user.getAge());
        out.writeBytes(bytes);
    }
}
