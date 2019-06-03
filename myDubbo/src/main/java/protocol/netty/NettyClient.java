package protocol.netty;

import framework.ClientProtocol;
import framework.Invocation;
import framework.URL;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class NettyClient implements ClientProtocol {
    //每次初始化都需要创建大量资源比较消耗资源，这里可以创建线程池
    public Object send(URL url, Invocation invocation) {
        ChannelFuture cf = ChannelFutureDriven.getInstance().getChannelFuture(url);
        cf.channel().writeAndFlush(invocation);
        Object object = ChannelFutureDriven.getInstance().getResult();
        cf.addListener(ChannelFutureListener.CLOSE);
        return object;
    }
}
