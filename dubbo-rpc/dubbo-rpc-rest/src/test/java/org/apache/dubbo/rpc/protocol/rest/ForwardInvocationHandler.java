package org.apache.dubbo.rpc.protocol.rest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 简单代理转发调用
 * @author dreamquster163
 * @create 2019/9/11 10:42 AM
 */
public class ForwardInvocationHandler implements InvocationHandler {
    private Object targetObject;

    public ForwardInvocationHandler(Object targetObject) {
        this.targetObject = targetObject;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(targetObject, args);
    }
}
