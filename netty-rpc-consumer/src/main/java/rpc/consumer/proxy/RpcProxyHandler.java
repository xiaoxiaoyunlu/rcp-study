package rpc.consumer.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class RpcProxyHandler extends ChannelInboundHandlerAdapter {

	//返回信息包response
	private Object response;
	
	public Object getResponse(){
		return response;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		response=msg;
//		System.out.println("Client 接收到的服务器的返回信息："+msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
//		super.exceptionCaught(ctx, cause);
		System.out.println("Client exception is general");
	}
	
}
