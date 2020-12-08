package org.example.api;

/**
 * @author 罗涛
 * @title IMessageConverter
 * @date 2020/12/8 10:31
 */
public interface IMessageConverter {
    int port();

    int interval();

    String printMsg();

    String name();
}
