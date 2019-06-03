package provider;

import java.util.HashMap;
import java.util.Map;

public class LocalMap {
    private static Map<String,Class<?>> REGISTER = new HashMap<String,Class<?>>();
    public static void register(String interfaceName,Class<?> clazz){
        REGISTER.put(interfaceName,clazz);
    }

    public static Class<?> getImplClass(String interfaceName){
        return REGISTER.get(interfaceName);
    }
}
