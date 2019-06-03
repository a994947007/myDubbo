package consumer;

import config.ProviderConfig;
import framework.URL;
import javafx.scene.chart.PieChart;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class Register {
    private static Register register = null;
    private static Map<String, List<URL>> REGISTER = new HashMap<String, List<URL>>();
    private ZooKeeper zooKeeper = null;
    private Register(){}
    static{
        if(register == null){
            synchronized (Register.class){
                if(register == null){
                    register = new Register();
                    register.init();
                }
            }
        }
    }

    public static Register getInstance(){
        return register;
    }

    public void init(){
        String zkUrl = ProviderConfig.getInstance().getConfig("zookeeperUrl");
        final CountDownLatch latch = new CountDownLatch(1);
        zooKeeper = null;
        try {
            zooKeeper = new ZooKeeper(zkUrl, 5000, new Watcher() {
                public void process(WatchedEvent event) {
                    latch.countDown();
                }
            });
            latch.await();
            //创建目录
            if(zooKeeper.exists("/dubbo",false) == null){
                zooKeeper.create("/dubbo","0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            if(zooKeeper.exists("/dubbo/register",false) == null){
                zooKeeper.create("/dubbo/register","0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
            }
        } catch (KeeperException e){
            e.printStackTrace();
        }catch (InterruptedException e){
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public URL discovery(String interfaceName){
        //先判断本地缓存是否拥有，若没有，则去注册中心获取，否则直接random()算法
        if(REGISTER.get(interfaceName) == null){
            try {
                String path = "/dubbo/register/" + interfaceName;
                List<String> list = zooKeeper.getChildren(path,new RegisterWatcher(interfaceName));
                List<URL> services = null;
                if(list.size() > 0){
                    services = Collections.synchronizedList(new ArrayList<URL>());
                    for (String str : list){
                        byte bs[] = zooKeeper.getData(path + "/" + str,new DataChangeWatcher(interfaceName,str),null);
                        JSONObject jsonObject = JSONObject.fromObject(new String(bs));
                        services.add((URL) JSONObject.toBean(jsonObject,URL.class));
                    }
                }
                REGISTER.put(interfaceName,services);
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return random(interfaceName);
    }

    private class DataChangeWatcher implements Watcher{
        private String interfaceName = null;
        private String hostname = null;
        public DataChangeWatcher(String interfaceName,String hostname){
            this.interfaceName = interfaceName;
            this.hostname = hostname;
        }

        public void process(WatchedEvent event) {
            if(event.getType() == Event.EventType.NodeDataChanged){
                List<URL> list = REGISTER.get(interfaceName);
                Iterator<URL> iterator = list.iterator();
                for(;iterator.hasNext();) {
                    URL url = iterator.next();
                    if(url.getHostname().equals(hostname)){
                        iterator.remove();
                    }
                }
                String path = "/dubbo/register/" + interfaceName;
                try {
                    byte [] bs = zooKeeper.getData(path + "/" + hostname,false,null);
                    JSONObject jsonObject = JSONObject.fromObject(new String(bs));
                    list.add((URL) JSONObject.toBean(jsonObject,URL.class));
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(event.getPath());
            }else if(event.getType() == Event.EventType.NodeDeleted){
                Iterator<URL> iterator = REGISTER.get(interfaceName).iterator();
                for(;iterator.hasNext();){
                    URL url = iterator.next();
                    if(url.getHostname().equals(hostname)){
                        iterator.remove();
                    }
                }
            }
        }
    }

    //用来监听结点服务是否存在，如果服务挂掉，从对应的列表中移除
    private class RegisterWatcher implements Watcher {
        private String interfaceName = null;
        public RegisterWatcher(String interfaceName){
            this.interfaceName = interfaceName;
        }
        public void process(WatchedEvent event) {
            if(event.getType() == Event.EventType.NodeChildrenChanged){
                List<URL> list = REGISTER.get(interfaceName);
                if(list != null){
                    list.clear();
                }
                REGISTER.remove(interfaceName);
            }
        }
    }

    //负载均衡
    public URL random(String interfaceName){
        List<URL> list = REGISTER.get(interfaceName);
        if(list == null)return null;
        return list.get((int)(Math.random() * list.size()));
    }

    public void close(){
        try {
            if(zooKeeper != null){
                zooKeeper.close();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
