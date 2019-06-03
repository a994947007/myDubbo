package protocol.http;

import framework.ClientProtocol;
import framework.Invocation;
import framework.URL;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class HttpClient implements ClientProtocol {
    public Object send(URL hostmsg, Invocation invocation) {
        //使用HttpClient发送请求
        OutputStream os = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
            java.net.URL url = new java.net.URL("http",hostmsg.getHostname(),hostmsg.getPort(),"/");
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoOutput(true);
            os = httpConnection.getOutputStream();
            oos = new ObjectOutputStream(os);
            oos.writeObject(invocation);
            oos.flush();
            ois = new ObjectInputStream(httpConnection.getInputStream());
            Object object = ois.readObject();
            return object;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(oos != null) oos.close();
                if(os != null) os.close();
                if(ois != null) ois.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
