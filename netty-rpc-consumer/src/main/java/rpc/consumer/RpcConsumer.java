package rpc.consumer;

import rpc.consumer.proxy.RpcProxy;

import com.zj.netty.api.api.IRpcCalc;
import com.zj.netty.api.api.IRpcHello;

public class RpcConsumer {

	public static void main(String[] args) {

		IRpcHello rpcHello=RpcProxy.create(IRpcHello.class);
		System.out.println(rpcHello.sayHello("zj"));
		
		IRpcCalc rpcCalc=RpcProxy.create(IRpcCalc.class);
		System.out.println("8+2="+rpcCalc.add(8, 2));
		
	}

}
