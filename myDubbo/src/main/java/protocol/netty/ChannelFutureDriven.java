package protocol.netty;

import framework.URL;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
//在调用的时候初始化，在comsumer客户端关闭的时候关闭资源
public class ChannelFutureDriven {
    private static ChannelFutureDriven instance = null;
    private ChannelFutureDriven(){}
    private EventLoopGroup group = new NioEventLoopGroup();
    private Bootstrap bootstrap = new Bootstrap();
    private ChannelFuture cf = null;
    private Object result = null;
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    static {
        if(instance == null){
            synchronized (ChannelFutureDriven.class){
                if(instance == null){
                    instance = new ChannelFutureDriven();
                    instance.init();
                }
            }
        }
    }

    public static ChannelFutureDriven getInstance(){
        return instance;
    }

    public Object getResult(){
        lock.lock();
        try {
            while(result == null){
                condition.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
        return result;
    }

    public void init(){
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new ReadTimeoutHandler(10));
                        pipeline.addLast(MarshallingCodeFactory.buildMarshallingDecoder());
                        pipeline.addLast(MarshallingCodeFactory.buildMarshallingEncoder());
                        pipeline.addLast(new NettyClientHandler());
                    }
                });
    }

    public ChannelFuture createChannelFuture(URL url){
        try {
            return bootstrap.connect(url.getHostname(),url.getPort()).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ChannelFuture getChannelFuture(URL url){
        if(this.cf == null){
            this.cf = createChannelFuture(url);
        }else if(this.cf.channel().isActive()){
            this.cf = createChannelFuture(url);
        }
        return this.cf;
    }

    private class NettyClientHandler extends SimpleChannelInboundHandler<Object> {
        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
            lock.lock();
            try {
                result = o;
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }

    public void shutdownGracefully(){
        group.shutdownGracefully();
    }
}
