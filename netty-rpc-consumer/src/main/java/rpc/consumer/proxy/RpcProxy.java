package rpc.consumer.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.zj.netty.api.msgbean.InvokerMsg;

/**
 * 客户端rpc代理
 * @author USER
 *
 */
public class RpcProxy {
	
	//api的接口类型
	//创建一个实际代理处理对象的方法

	@SuppressWarnings("unchecked")
	public static <T> T create(Class<?> clazz){
		//MethodProxy：静态代理
		MethodProxy methodProxy=new MethodProxy(clazz);
		//result:rpcInvoke
		T result=(T)Proxy.newProxyInstance(clazz.getClassLoader(),new Class[]{clazz},methodProxy);
		return result;
		
	}
}	
	
	
	
	
	/**
	 * 这里是实现InvocationHandler的动态代理类
	 * @author USER
	 *
	 */
	class MethodProxy implements InvocationHandler {

		private Class<?> clazz;
		
		public MethodProxy(Class<?> clazz) {
			this.clazz=clazz;
		}
		
		public Object rpcInvoke(Object proxy,Method method,Object[] args){
			//模拟远程websocket信息包数据
			InvokerMsg msg=new InvokerMsg();
			msg.setClassName(this.clazz.getName());
			msg.setMethodName(method.getName());
			msg.setParames(method.getParameterTypes());
			msg.setValues(args);
			
			//使用netty来处理
			final RpcProxyHandler consumerHandler=new RpcProxyHandler();
			//Netty做一个远程异步传输
			
			EventLoopGroup group=new NioEventLoopGroup();
			try {
				Bootstrap b=new Bootstrap();
				b.group(group).channel(NioSocketChannel.class)
				    .option(ChannelOption.TCP_NODELAY, true)
				    .handler(new ChannelInitializer<Channel>() {

						@Override
						protected void initChannel(Channel ch) throws Exception {
							
							ChannelPipeline pipeline=ch.pipeline();
							pipeline.addLast("frameDecoder",
									new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0,4,0,0));
							pipeline.addLast("frameEncoder",new LengthFieldPrepender(4));
							//底层传输是字节协议流  字节协议六转化我们的Netty处理对象
							//对象自动序列化和反序列化
							pipeline.addLast("encoder",new ObjectEncoder());
							pipeline.addLast("decoder",
									new ObjectDecoder(Integer.MAX_VALUE,ClassResolvers.cacheDisabled(null)));
						    //处理远程服务调用信息Handler
							pipeline.addLast(consumerHandler);
						}
					});
				
				ChannelFuture future=b.connect("localhost",8080).sync();
				//发送代理信息给远程服务器
				future.channel().writeAndFlush(msg).sync();
				future.channel().closeFuture().sync();
				
			} catch (Exception e) {
			    e.printStackTrace();
			}finally{
				group.shutdownGracefully();
				//netty做一个远程异步传输
			}
			return consumerHandler.getResponse();
		}


		/**
		 * InvocationHandler  代理委派器 invoke方法
		 * proxy 代理类
		 * method 接口的方法
		 * args 参数
		 */
		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			//如果传进来的是一个具体的实现类，则本次演示略过此逻辑
			//返回表示声明由此Method对象表示的类或者接口的Class对象
			if(Object.class.equals(method.getDeclaringClass())){
				try {
					return method.invoke(this, args);
				} catch (Exception e) {
					e.printStackTrace();
				}
				//如果传进来的是一个接口（核心）
			}else {
				//这里专门写远程调用分支    本地逻辑没有实现服务逻辑
				return rpcInvoke(proxy, method, args);
			}
			return null;
		}

	}



