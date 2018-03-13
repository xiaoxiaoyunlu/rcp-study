package rpc.provider;

import com.zj.netty.api.api.IRpcHello;

public class RpcHello implements IRpcHello {

	public String sayHello(String name) {
          return "Hello "+name+"!";
	}

}
