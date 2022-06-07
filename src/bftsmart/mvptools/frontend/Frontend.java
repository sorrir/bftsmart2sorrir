package bftsmart.mvptools.frontend;

import java.io.IOException;
import java.net.InetSocketAddress;

import bftsmart.mvptools.sorrirmessage.SorrirMessageDecoder;
import bftsmart.tom.AsynchServiceProxy;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;


public class Frontend {

    static int initId;

    public static void main(String[] args) throws IOException {
        System.out.println("Frontend, v0.1bench");
        if (args.length < 3) {
            System.out.println("Usage: ... Frontend <initial client id> <listening on port (number)> <configPath> <verbose?> <nosig | default | ecdsa>");
            System.exit(-1);
        }

        initId = Integer.parseInt(args[0]);
        int port = Integer.parseInt(args[1]);
        String configPath = args[2];
        boolean verbose = Boolean.parseBoolean(args[3]);
        String sign = args[4];

        int s = 0;
        if (!sign.equalsIgnoreCase("nosig")) s++;
        if (sign.equalsIgnoreCase("ecdsa")) s++;

        /*
        if (s == 2 && Security.getProvider("SunEC") == null) {

            System.out.println("Option 'ecdsa' requires SunEC provider to be available.");
            System.exit(0);
        }
        */

        System.out.println("Launching frontend ");
        Client client = new Client(initId, port, configPath, verbose, s);

        System.out.println("Frontend initialized");

        client.start();
    }


    static class Client extends Thread {

        int id;
        String configPath;
        AsynchServiceProxy serviceProxy;
        boolean verbose;
        int port;

        public Client(int id, int port, String configPath, boolean verbose, int sign) {

            this.id = id;
            this.configPath = configPath;
            this.port = port;
            this.serviceProxy = new AsynchServiceProxy(id, configPath);
            this.verbose = verbose;
        }

        public void run() {

            EventLoopGroup group = new NioEventLoopGroup();
            String address = "127.0.0.1";

            try {
                ServerBootstrap serverBootstrap = new ServerBootstrap();
                serverBootstrap.group(group);
                serverBootstrap.channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new SorrirMessageDecoder());
                        ch.pipeline().addLast(new FrontEndEventHandler(serviceProxy));
                    }
                }).childOption(ChannelOption.SO_KEEPALIVE, true).childOption(ChannelOption.TCP_NODELAY, true);
                serverBootstrap.localAddress(new InetSocketAddress(address, this.port));

                System.out.print("Listening on tcp://" + address + ":" + port);

                ChannelFuture channelFuture = serverBootstrap.bind().sync();
                channelFuture.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    group.shutdownGracefully().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

