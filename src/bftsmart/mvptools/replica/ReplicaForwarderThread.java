package bftsmart.mvptools.replica;

import bftsmart.mvptools.sorrirmessage.SorrirMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

public class ReplicaForwarderThread extends Thread {

    ChannelFuture channelFuture;
    int mvpPort;

    public ReplicaForwarderThread(int mvpPort) {
        this.mvpPort = mvpPort;
    }

    public void run() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.group(group);
            clientBootstrap.channel(NioSocketChannel.class);
            clientBootstrap.remoteAddress(new InetSocketAddress("127.0.0.1", mvpPort));
            clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new SorrirMessageEncoder());
                    socketChannel.pipeline().addLast(new ReplicaForwarderThreadHandler());
                }
            });
            ChannelFuture channelFuture = clientBootstrap.connect().sync();
            this.channelFuture = channelFuture;
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void send(Object message) {
       // System.out.println("ReplicaForwarderThread .send() " + message);
        channelFuture.channel().writeAndFlush(message);
    }
}
