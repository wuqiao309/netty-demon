package wqcdm.top;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import wqcdm.top.hadler.ClientChatHandler;
import wqcdm.top.hadler.ClientHeartBeartHandler;

/**
 * @author wuqiao
 * Created on 2022-05-28
 */

public class Client {
    EventLoopGroup group;
    Bootstrap bootstrap;
    volatile Channel channel;

    private void init() {
        group = new NioEventLoopGroup(10);
        Client client = this;
        bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline()
                                .addLast(new LineBasedFrameDecoder(4096))
                                .addLast(new StringDecoder())
                                .addLast(new StringEncoder())
                                .addLast(new IdleStateHandler(0, 5, 0))
                                .addLast(new ClientHeartBeartHandler(client))
                                .addLast(new ClientChatHandler())
                        ;
                    }
                });
    }

    // 连接服务器
    public boolean connect() {
        System.out.println("netty client connecting ...");
        ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 9000);
        channelFuture.addListener((ChannelFutureListener) future -> {
            channel = future.channel();
            if (future.isSuccess()) {
                System.out.println("netty client connect success");
                channel.writeAndFlush(String.format("user:%s\n", ThreadLocalRandom.current().nextInt(100)));

                channel.eventLoop().scheduleAtFixedRate(() -> {
                    channel.writeAndFlush(UUID.randomUUID() + "\n");
                }, 0, 5, TimeUnit.SECONDS);
            } else {
                System.err.println("netty client connect failed");
            }
        });
        boolean notTimeout = channelFuture.awaitUninterruptibly(10, TimeUnit.SECONDS);
        Channel channel1 = channelFuture.channel();
        if (notTimeout) {
            if (channel1 != null && channel1.isActive()) {
                channel = channel1;
                return true;
            }
        }
        if (channel1 != null && channel1.isActive()) {
            channel.close();
        }
        return false;
    }

    // private void sendMsg() {
    //     group.schedule(() -> {
    //         System.out.println("发送消息");
    //         channelFuture.channel().writeAndFlush(UUID.randomUUID() + "\n");
    //         sendMsg();
    //     }, 1, TimeUnit.SECONDS);
    // }

    public static void main(String[] args) throws InterruptedException {
        Client client = new Client();
        try {
            client.init();
            while (true) {
                boolean connect = client.connect();
                if (connect) {
                    client.channel.closeFuture().sync();
                }
            }
        } catch (InterruptedException e) {
            if (client.group != null) {
                client.group.shutdownGracefully();
            }
        }
    }
}


