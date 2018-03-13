package rpc.registry;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.zj.netty.api.msgbean.InvokerMsg;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class RegistryHandler extends ChannelInboundHandlerAdapter {

	//接口的名字 和接口调用地址，用OBject代理：http RMI   代替 ZK
	public static ConcurrentHashMap<String, Object> registryMap=new ConcurrentHashMap<String, Object>();
	
	//全限定类名：rpc.provider.RpcCalc
	private List<String> classCache=new ArrayList<String>();
	
	public RegistryHandler(){
		//springIoc
		//服务的发现配置服务地址
		// dubbo regest="192.168.1.23";
		//通过spring方式找到服务地址，引入SpringIoc
		//自动寻找服务提供地址init
		scannerClass("rpc.provider");
		//注册服务
		doRegistry();
	}

	/**
	 * 服务全局注册，用map代替ZK 或者  服务注册中心的服务
	 */
	private void doRegistry() {

		//没有可注册的服务
		if(classCache.size()==0){
			return ;
		}
		
		for(String className:classCache){
           try {
			  Class<?> classStr=Class.forName(className);
			  Class<?> interfaces=classStr.getInterfaces()[0];
			  //registryMap 服务注册模块
			  registryMap.put(interfaces.getName(),classStr.newInstance());
		} catch (Exception e) {
			e.printStackTrace();
		}			
		}

	}

	/**
	 * 服务扫描发现
	 * @param string
	 */
	private void scannerClass(String packageName) {

		URL url=this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
		File dir=new File(url.getFile());
		for(File file:dir.listFiles()){
			if(file.isDirectory()){
				scannerClass(packageName+"."+file.getName());
			}else {
				//找到全包类名  rpc.provider.RpcCalc
				classCache.add(packageName+"."+file.getName().replace(".class", "").trim());
			}
			
		}
	}

	/**
	 * 重写这个channelRead来读取远程调用信息（Consumer的代理发送过的信息包）
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		Object result=new Object();
		InvokerMsg request=(InvokerMsg) msg;
		//远程调用发现服务
		if(registryMap.containsKey(request.getClassName())){
			//获取接口名称
			Object clazz=registryMap.get(request.getClassName());
			// add 改成服务走http请求？
			//代理对象里面方法名称和方法参数
			Method method=clazz.getClass().getMethod(request.getMethodName(), request.getParames());
            //执行method的invoke 相当于执行方法   
			result=method.invoke(clazz, request.getValues());
		}
		//异步的方式会写给客户端
		ctx.write(result);
		ctx.flush();
		ctx.close();
	}
	
	
	
}
