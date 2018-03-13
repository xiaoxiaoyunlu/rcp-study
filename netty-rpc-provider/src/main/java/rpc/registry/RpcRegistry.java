package rpc.registry;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * 服务注册模块 服务启动的时候，会发现服务注册模块 远程的Request 解析出需要请求的信息 然后请求信息找到注册这个服务 然后返回response
 * 
 * @author USER
 *
 */
public class RpcRegistry {
	// 接收netty传输过来的信息包
	private int port;

	public RpcRegistry(int port) {
		this.port = port;
	}

	/**
	 * 接收代理传进来的Request信息包
	 */
	public void start() {
		// 构建netty启动的Boss线程池
		EventLoopGroup boosGroup = new NioEventLoopGroup();
		// 构建netty启动的worker线程池
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			// netty启动辅助类
			ServerBootstrap b = new ServerBootstrap();
			b.group(boosGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel socketChannel)
								throws Exception {
							//初始化责任链条（插入很多Handler生存的容器）
                            ChannelPipeline pipeline=socketChannel.pipeline();
                            //加入编码器，公共预处理
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4,0,4));
                            pipeline.addLast("encoder",new ObjectEncoder());
                            pipeline.addLast("decoder", 
                            		new ObjectDecoder(Integer.MAX_VALUE,ClassResolvers.cacheDisabled(null)));
						   //处理远程代理传过来的信息的处理handler
                            pipeline.addLast(new RegistryHandler());
						}
					}).option(ChannelOption.SO_BACKLOG, 128)
					.childOption(ChannelOption.SO_KEEPALIVE,true);

			     ChannelFuture future =b.bind(port).sync();
			     System.out.println("RPC registry start listen at "+port);
			     future.channel().closeFuture().sync();
			
		} catch (Exception e) {
			e.printStackTrace();
			boosGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
	public static void main(String[] args) {
		new RpcRegistry(8080).start();
	}
}
