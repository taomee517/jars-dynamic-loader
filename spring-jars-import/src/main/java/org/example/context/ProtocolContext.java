package org.example.context;

import org.example.api.IMessageConverter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * @author 罗涛
 * @title ProtocolContext
 * @date 2020/5/8 14:42
 */
@Component
public class ProtocolContext {
    public static final String BASE_PATH = "file:D:/sdk/jars";
    private Map<Integer, IMessageConverter> PORT_PROTOCOL_MAP = new HashMap<>();
    public Set<IMessageConverter> tcpConverters = new HashSet<>();

    @PostConstruct
    private void getAllProtocols(){
        ServiceLoader<IMessageConverter> loader = ServiceLoader.load(IMessageConverter.class);
        Iterator<IMessageConverter> iterator = loader.iterator();
        while (iterator.hasNext()){
            IMessageConverter converter = iterator.next();
            PORT_PROTOCOL_MAP.put(converter.port(),converter);
            tcpConverters.add(converter);
        }
    }

    public IMessageConverter getMessageConverterByPort(int port){
        return PORT_PROTOCOL_MAP.get(port);
    }

}
