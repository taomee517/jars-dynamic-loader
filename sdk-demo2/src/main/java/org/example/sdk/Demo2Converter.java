package org.example.sdk;

import org.example.api.IMessageConverter;

/**
 * @author 罗涛
 * @title sdk
 * @date 2020/12/8 10:42
 */
public class Demo2Converter implements IMessageConverter {

    @Override
    public int port() {
        return 33166;
    }

    @Override
    public int interval() {
        return 10;
    }

    @Override
    public String printMsg() {
        return "This is SDK-2, from L.A,USA!";
    }

    @Override
    public String name() {
        return "SDK-2";
    }
}
