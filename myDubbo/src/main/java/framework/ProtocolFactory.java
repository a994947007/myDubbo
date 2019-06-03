package framework;

import protocol.http.HttpClient;
import protocol.http.HttpServer;
import protocol.netty.NettyClient;
import protocol.netty.NettyServer;

public class ProtocolFactory {
    public static ClientProtocol getClientProtocol(){
        String protocolName = System.getProperty("protocolName");
        if(protocolName == null || protocolName.trim().equals("")) protocolName = "http";
        if(protocolName.trim().equals("http")){
            return new HttpClient();
        }else if(protocolName.trim().equals("netty")){
            return new NettyClient();
        }
        return new HttpClient();
    }

    public static ServerProtocol getServerProtocol(){
        String protocolName = System.getProperty("protocolName");
        if(protocolName == null || protocolName.trim().equals("")) protocolName = "http";
        if(protocolName.trim().equals("http")){
            return new HttpServer();
        }else if(protocolName.trim().equals("netty")){
            return new NettyServer();
        }
        return new HttpServer();
    }
}
