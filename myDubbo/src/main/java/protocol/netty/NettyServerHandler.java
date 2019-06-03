package protocol.netty;

import framework.Invocation;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import provider.LocalMap;

public class NettyServerHandler extends SimpleChannelInboundHandler<Invocation> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Invocation invocation) throws Exception {
        Class<?> clazz = LocalMap.getImplClass(invocation.getInterfaceName());
        Object object = clazz.getMethod(invocation.getMethodName(),invocation.getParamTypes())
                .invoke(clazz.newInstance(),invocation.getParams());
        channelHandlerContext.writeAndFlush(object);
    }
}
