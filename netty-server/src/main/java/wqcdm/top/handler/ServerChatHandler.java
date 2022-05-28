package wqcdm.top.handler;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author wuqiao
 * Created on 2022-05-28
 */
public class ServerChatHandler extends ChannelHandlerAdapter {
    String user = "";

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String msg = String.format("[%s]下线了\n", user);
        System.out.print(msg);
        ServerHeartBeatHandler.channelGroup.writeAndFlush(msg);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg != null && msg.toString().startsWith("user:")) { // 相当于登录
            user = msg.toString().replaceFirst("user:", "");
            String message = String.format("[%s]上线了\n", user);
            System.out.print(message);
            ServerHeartBeatHandler.channelGroup.writeAndFlush(message);
        } else if (user == "") {
            String message = "你还没登录\n";
            System.out.print(message);
            ctx.channel().writeAndFlush(message);
        } else  {
            String massage = "[%s]说：%s\n";
            String otherMessage = String.format(massage, user, msg);
            String selfMessage = String.format(massage, "我", msg);
            System.out.print(otherMessage);
            ServerHeartBeatHandler.channelGroup.forEach(ch -> {
                if (ch == ctx.channel()) {
                    ch.writeAndFlush(selfMessage);
                } else {
                    ch.writeAndFlush(otherMessage);
                }
            });
        }
    }
}
