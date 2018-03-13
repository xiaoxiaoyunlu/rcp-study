package rpc.provider;

import com.zj.netty.api.api.IRpcCalc;

public class RpcCalc implements IRpcCalc {

	public int add(int a, int b) {
		return a+b;
	}

}
