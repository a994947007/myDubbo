package consumer;

import framework.ClientProtocol;
import framework.Invocation;
import framework.ProtocolFactory;
import framework.URL;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyFactory {
    public static <T> T createProxy(final Class<?> interfaceClass){
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                ClientProtocol protocol = ProtocolFactory.getClientProtocol();
                Invocation invocation = new Invocation(interfaceClass.getName(),method.getName(),method.getParameterTypes(),args);
                //去zookeeper注册中心获取服务URL
                URL url = Register.getInstance().discovery(interfaceClass.getName());
                Object object = protocol.send(url,invocation);
                return object;
            }
        });
    }
}
