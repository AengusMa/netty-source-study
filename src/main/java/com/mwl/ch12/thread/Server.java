package com.mwl.ch12.thread;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;

import static com.mwl.ch12.thread.Constant.*;

/**
 * @author mawenlong
 * @date 2019/1/31
 */
public class Server {

    public static void main(String[] args) {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        EventLoopGroup businessGroup = new NioEventLoopGroup(1000);

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);


        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new FixedLengthFrameDecoder(Long.BYTES));
                //指定业务线程池，ServerBusinessHandler方法会在指定的线程池执行
                ch.pipeline().addLast(businessGroup, ServerBusinessHandler.INSTANCE);
                // ch.pipeline().addLast(ServerBusinessThreadPoolHandler.INSTANCE);
            }
        });


        bootstrap.bind(PORT).addListener((ChannelFutureListener) future -> System.out
                .println("bind success in port: " + PORT));
    }

}
