package com.anoyi.grpc.util;

import com.anoyi.grpc.constant.SerializeType;
import com.anoyi.grpc.service.SerializeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SerializeUtils {

    private final static Map<Integer, SerializeService> cachedMap = new ConcurrentHashMap<>();

    /**
     * 获取 序列化/反序列化 工具实例
     *
     * @param serializeType               序列化工具类型
     * @param defaultSerializationService 默认的序列化方式
     */
    public static SerializeService getSerializeService(SerializeType serializeType, SerializeService defaultSerializationService) {
        if (!StringUtils.isEmpty(serializeType)) {
            Integer value = serializeType.getValue();
            SerializeService cachedSerializationService = cachedMap.get(value);
            if (cachedSerializationService != null) {
                return cachedSerializationService;
            } else {
                try {
                    cachedSerializationService = (SerializeService) serializeType.getClazz().newInstance();
                    cachedMap.put(value, cachedSerializationService);
                } catch (InstantiationException | IllegalAccessException e) {
                    log.error("{} newInstance error, use default codecService." + serializeType.getClazz().getName());
                    cachedSerializationService = defaultSerializationService;
                }
                return cachedSerializationService;
            }
        } else {
            return defaultSerializationService;
        }
    }

    /**
     * 获取 序列化/反序列化 工具实例
     *
     * @param value                       序列化工具类型
     * @param defaultSerializationService 默认的序列化方式
     */
    public static SerializeService getSerializeService(int value, SerializeService defaultSerializationService) {
        SerializeType serializeType = SerializeType.getSerializeTypeByValue(value);
        return getSerializeService(serializeType, defaultSerializationService);
    }

}
