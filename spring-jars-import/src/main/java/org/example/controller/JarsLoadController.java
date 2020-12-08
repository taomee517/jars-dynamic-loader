package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.api.IMessageConverter;
import org.example.context.ProtocolContext;
import org.example.server.NettyServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author 罗涛
 * @title IndexController
 * @date 2020/12/8 10:02
 */

@Slf4j
@RestController
@RequestMapping("jars")
public class JarsLoadController {

    @Autowired
    NettyServer nettyServer;

    @PostMapping("load")
    public void load(String jarName, String converterClass){
        try {
            String path = StringUtils.joinWith("/", ProtocolContext.BASE_PATH, jarName);
            URL jarUrl = new URL(path);
            URLClassLoader myClassLoader = new URLClassLoader(new URL[] { jarUrl }, Thread.currentThread()
                    .getContextClassLoader());
            Class<?> converterClz = myClassLoader.loadClass(converterClass);
            IMessageConverter converter = (IMessageConverter)converterClz.newInstance();
            String name = converter.name();
            log.info("load SDK:{}", name);
            nettyServer.startSingleTcpServer(converter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @PostMapping("unload")
    public void unload(String jarName, String converterClass){
        try {
            String path = StringUtils.joinWith("/", ProtocolContext.BASE_PATH, jarName);
            URL jarUrl = new URL(path);
            URLClassLoader myClassLoader = new URLClassLoader(new URL[] { jarUrl }, Thread.currentThread()
                    .getContextClassLoader());
            Class<?> converterClz = myClassLoader.loadClass(converterClass);
            IMessageConverter converter = (IMessageConverter)converterClz.newInstance();
            String name = converter.name();
            log.info("unload SDK:{}", name);
            int port = converter.port();
            nettyServer.closeSingleChannel(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
