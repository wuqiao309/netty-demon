package wqcdm.top;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import wqcdm.top.handler.ServerChatHandler;
import wqcdm.top.handler.ServerHeartBeatHandler;

/**
 * @author wuqiao
 * Created on 2022-05-28
 */
public class Server {
    public static void main(String[] args) throws InterruptedException {
        // 创建两个线程组。线程组默认数量为cpu核数的两倍
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new LineBasedFrameDecoder(4096))
                                    .addLast(new StringDecoder())
                                    .addLast(new StringEncoder())
                                    .addLast(new IdleStateHandler(10, 0, 0))
                                    .addLast(new ServerHeartBeatHandler())
                                    .addLast(new ServerChatHandler())
                            ;
                        }
                    });
            System.out.println("netty server start。。");
            ChannelFuture cf = bootstrap.bind(9000).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    System.out.println("netty server start success");
                } else {
                    System.out.println("netty server start failed");
                }
            }).sync();
            cf.channel().closeFuture().addListener(
                    (ChannelFutureListener) future -> ServerHeartBeatHandler.channelGroup.forEach(channel -> channel.close())).sync();
        }  finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
