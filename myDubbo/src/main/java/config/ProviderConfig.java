package config;

import java.io.IOException;
import java.util.Properties;

public class ProviderConfig {
    private static ProviderConfig config = null;
    private Properties properties = new Properties();
    static {
        if(config == null){
            synchronized (ProviderConfig.class){
                if(config == null){
                    config = new ProviderConfig();
                    try {
                        config.properties.load(ProviderConfig.class.getClassLoader().getResourceAsStream("provider.properties"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static ProviderConfig getInstance(){
        return config;
    }

    public String getConfig(String name){
        return properties.getProperty(name);
    }
}
