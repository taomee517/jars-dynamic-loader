package org.example.sdk;

import org.example.api.IMessageConverter;

/**
 * @author 罗涛
 * @title sdk
 * @date 2020/12/8 10:42
 */
public class Demo1Converter implements IMessageConverter {

    @Override
    public int port() {
        return 33155;
    }


    @Override
    public int interval() {
        return 20;
    }

    @Override
    public String printMsg() {
        return "This SDK-1, from Chongqing,China!";
    }

    @Override
    public String name() {
        return "SDK-1";
    }
}
