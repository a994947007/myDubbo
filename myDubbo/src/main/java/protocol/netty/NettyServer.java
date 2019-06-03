package protocol.netty;

import framework.ServerProtocol;
import framework.URL;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class NettyServer implements ServerProtocol {
    public void start(URL url) {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss,worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,1024)
                .option(ChannelOption.SO_RCVBUF,32 * 1024)
                .option(ChannelOption.SO_RCVBUF,32 * 1024)
                .childOption(ChannelOption.SO_KEEPALIVE,true);
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                pipeline.addLast(new ReadTimeoutHandler(10));
                pipeline.addLast(MarshallingCodeFactory.buildMarshallingDecoder());
                pipeline.addLast(MarshallingCodeFactory.buildMarshallingEncoder());
                pipeline.addLast(new NettyServerHandler());
            }
        });

        try {
            ChannelFuture cf = serverBootstrap.bind(url.getPort()).sync();
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
