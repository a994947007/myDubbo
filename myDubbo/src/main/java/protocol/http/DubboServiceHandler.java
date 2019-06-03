package protocol.http;

import framework.Invocation;
import provider.LocalMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

public class DubboServiceHandler {

    public void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectInputStream ois = new ObjectInputStream(req.getInputStream());
        ObjectOutputStream oos = new ObjectOutputStream(resp.getOutputStream());
        try {
            Invocation invocation = (Invocation)ois.readObject();
            Class<?> clazz = LocalMap.getImplClass(invocation.getInterfaceName());
            Object object = clazz.getMethod(invocation.getMethodName(),invocation.getParamTypes()).invoke(clazz.newInstance(),invocation.getParams());
            oos.writeObject(object);
            oos.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
