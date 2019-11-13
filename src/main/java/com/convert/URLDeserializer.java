package com.convert;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author 12130
 * @date 2019/11/13
 * @time 21:10
 */
public class URLDeserializer implements ObjectDeserializer {
    @Override
    public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object fieldName) {
        Object parse = defaultJSONParser.parse(fieldName);
        if (parse == null) {
            return null;
        }
        try {
            return (T) new URL((String) parse);
        } catch (MalformedURLException ignore) {
            return null;
        }
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }
}
