package framework;

public class URL {
    private String hostname;
    private int port;
    public URL(){}

    public URL(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean equals(URL url){
        return this.hostname.equals(url.hostname) &&  this.port == url.port;
    }

}
