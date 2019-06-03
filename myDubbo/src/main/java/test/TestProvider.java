package test;

import provider.Provider;

import java.io.File;

public class TestProvider {
    public static void main(String[] args) {
        Provider provider = new Provider();
        provider.init();
        //System.out.println(Provider.class.getClassLoader().getResource("test").getFile());;
    }
}
