package wqcdm.top.handler;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author wuqiao
 * Created on 2022-05-28
 */
public class ServerHeartBeatHandler extends ChannelHandlerAdapter {
    public static final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    int idleCount = 0;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        channelGroup.add(ctx.channel());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        channelGroup.remove(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("收到消息：" + msg);
        if ("heart".equals(msg)) {
            ctx.writeAndFlush("heartok\n");
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleStateEvent event = (IdleStateEvent) evt;
        idleCount++;
        System.out.println(String.format("%s第%s次%s心跳超时", ctx.channel(), idleCount, event.state().name()));
        if (idleCount == 3) {
            System.out.println(String.format("%s心跳超时超过限制，断开连接", ctx.channel()));
            ctx.writeAndFlush("timeout\n");
            ctx.channel().close();
        }
    }


}
