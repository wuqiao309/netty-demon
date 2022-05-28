package wqcdm.top.hadler;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import wqcdm.top.Client;

/**
 * @author wuqiao
 * Created on 2022-05-28
 */
public class ClientHeartBeartHandler extends ChannelHandlerAdapter {

    Client client;
    public ClientHeartBeartHandler(Client client) {
        this.client = client;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if ("timeout".equals(msg)) {
            System.out.println("server认为超时了");
        } else if ("heartok".equals(msg)) {
            return;
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel变为inactive");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        System.out.println("该发心跳了");
        ctx.writeAndFlush("heart\n");
    }
}
