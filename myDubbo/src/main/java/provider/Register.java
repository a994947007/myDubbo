package provider;

import config.ProviderConfig;
import framework.URL;
import net.sf.json.JSONObject;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class Register {
    private static Register register = null;
    private ZooKeeper zooKeeper = null;
    private Register(){}
    static {
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
                zooKeeper.create("/dubbo","0".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
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

    public void register(String interfaceName,URL url){
        try {
            if(zooKeeper.exists("/dubbo/register/" + interfaceName,false) == null){
                zooKeeper.create("/dubbo/register/" + interfaceName,"".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
            }
            String strUrl = url.getHostname();
            JSONObject jsonUrl = JSONObject.fromObject(url);
            if(zooKeeper.exists("/dubbo/register/" + interfaceName + "/" + strUrl,false) == null){
                zooKeeper.create("/dubbo/register/" + interfaceName + "/" + strUrl,jsonUrl.toString().getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
