package test;

import consumer.ProxyFactory;

public class TestConsumer {
    public static void main(String[] args) {
        UserService service = ProxyFactory.createProxy(UserService.class);
        System.out.println(service.sayHello("abc"));
        System.out.println(service.sayHello("bcd"));
    }
}
