package provider;

import config.ProviderConfig;
import framework.ProtocolFactory;
import framework.ServerProtocol;
import framework.URL;
import framework.annotation.Service;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class Provider {
    private Map<String,Class<?>> classes = new HashMap<String,Class<?>>();

    public void init(){
        //获取需要注册的类
        doScanPacket();
        //注册远程服务(Redis,zookeeper)
        doRegisterZK();
        //注册到本地服务
        doRegisterLocal();
        //开放服务
        doOpenService();
    }

    //注册到注册中心
    public void doRegisterZK(){
        Register register = Register.getInstance();
        URL url = new URL(ProviderConfig.getInstance().getConfig("hostname"),
                Integer.parseInt(ProviderConfig.getInstance().getConfig("port")));
        for (Map.Entry<String,Class<?>> entry : classes.entrySet()){
            register.register(entry.getKey(),url);
        }
    }

    //开放服务
    public void doOpenService(){
        ServerProtocol protocol = ProtocolFactory.getServerProtocol();
        String hostname = ProviderConfig.getInstance().getConfig("hostname");
        int port = Integer.parseInt(ProviderConfig.getInstance().getConfig("port"));
        protocol.start(new URL(hostname,port));
    }

    //注册到本地服务
    public void doRegisterLocal(){
        for (Map.Entry<String,Class<?>> entry :classes.entrySet()){
            LocalMap.register(entry.getKey(),entry.getValue());
        }
    }

    //获取需要注册的类
    public void doScanPacket(){
        String packet = ProviderConfig.getInstance().getConfig("scanPacket");
        scanPacket(packet);
    }

    public void scanPacket(String packet){
        String path = packet.replaceAll("\\.","/");
        File file = null;
        try {
            file = new File(Provider.class.getClassLoader().getResource(path).toURI().getPath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        File files[] = file.listFiles();
        for(File f: files){
            if(f.isFile() && f.getName().endsWith(".class")){
                String className = f.getName().substring(0,f.getName().lastIndexOf("."));
                try {
                    Class<?> clazz = Class.forName(packet + "." + className);
                    if(clazz.isAnnotationPresent(Service.class))classes.put(packet + "." + clazz.getAnnotation(Service.class).interfaceName(),clazz);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }else if(f.isDirectory()){
                scanPacket(packet + "." + f.getName());
            }
        }
    }
}
