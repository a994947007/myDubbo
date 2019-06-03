package config;

import java.io.IOException;
import java.util.Properties;

public class ConsumerConfig {
    private static ConsumerConfig config = null;
    private Properties properties = new Properties();
    private ConsumerConfig(){}
    static {
        if(config == null){
            synchronized (ConsumerConfig.class){
                if(config == null){
                    config = new ConsumerConfig();
                    try {
                        config.properties.load(ConsumerConfig.class.getResourceAsStream("consumer.properties"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static ConsumerConfig getInstance(){
        return config;
    }

    public String getConfig(String name){
        return properties.getProperty(name);
    }

}
