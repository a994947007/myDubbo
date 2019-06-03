package test;

import framework.annotation.Service;

@Service(interfaceName = "UserService")
public class UserServiceImpl implements UserService{
    public String sayHello(String name) {
        return "hello " + name;
    }
}
