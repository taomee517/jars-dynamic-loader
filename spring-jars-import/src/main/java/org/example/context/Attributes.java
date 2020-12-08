package org.example.context;

import io.netty.util.AttributeKey;
import org.example.api.IMessageConverter;

/**
 * @author 罗涛
 * @title Attributes
 * @date 2020/12/8 10:56
 */
public interface Attributes {
    AttributeKey<IMessageConverter> PROTOCOL = AttributeKey.newInstance("PROTOCOL");
    AttributeKey<Integer> INTERVAL = AttributeKey.newInstance("INTERVAL");
}
