package com.zj.netty.api.msgbean;

import java.io.Serializable;
/**
 * 传输对象协议
 * Serializable标记接口才允许在网络中传输

 * @author USER
 *
 */
public class InvokerMsg implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1178968688789587918L;
	
	private String className;   //类名
	private String methodName;   //方法名
	private Class<?>[] parames;   //参数类型
	private Object[] values;      //参数列表
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public Class<?>[] getParames() {
		return parames;
	}
	public void setParames(Class<?>[] parames) {
		this.parames = parames;
	}
	public Object[] getValues() {
		return values;
	}
	public void setValues(Object[] values) {
		this.values = values;
	}

}
