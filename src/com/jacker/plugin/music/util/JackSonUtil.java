package com.jacker.plugin.music.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class JackSonUtil {
    public static final Logger log = Logger.getLogger("JackSonUtil");

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectMapper NOT_NULL_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        NOT_NULL_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static String convertJson(Object object) {
        String result = "";
        try {
            result = OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.warning("Convert json error! " + object + e.getMessage());
        }
        return result;
    }

    /**
     * json转clazz类型实例
     *
     * @param <T>
     * @param json
     * @param clazz 实例类型
     * @return clazz实例
     */
    public static <T> T convertJsonToBean(String json, Class<T> clazz) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            log.warning("convertJsonToBean failed!" + e.getMessage());
        }
        return null;
    }

    /**
     * Json转Map<String, Object>
     *
     * @param json
     * @return
     * @throws IOException
     */
    public static Map<String,Object> json2Map(String json) {
        try {
            return  OBJECT_MAPPER.readValue(json, Map.class);
        } catch (IOException e) {
            log.warning("json2Map failed!" + e.getMessage());
        }
        return null;
    }

    public static Map<String, String> json2StrMap(String json) {
        try {
            Map<String, Object> objMap = OBJECT_MAPPER.readValue(json, Map.class);
            if (objMap != null) {
                return objMap.entrySet().stream().collect(Collectors.toMap(entry->entry.getKey(), entry-> entry.getValue().toString()));
            }
        } catch (IOException e) {
            log.warning("json2StrMap failed!" + e.getMessage());
        }
        return null;
    }

    /**
     * json字符串转List
     *
     * @param json
     * @param elementClazz
     * @param <T>
     * @return
     */
    public static <T> List<T> jsonArrayToList(String json, Class<T> elementClazz) {
        if (json == null || elementClazz == null) {
            return null;
        }
        try {
            JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(ArrayList.class, elementClazz);
            return OBJECT_MAPPER.readValue(json, javaType);
        } catch (Exception e) {
            log.warning(String.format("jsonArrayToList failed! json: %s, elementClazz: %s", json, elementClazz) + e.getMessage());
        }
        return null;
    }


    /**
     * 实体类转换为map
     *
     * @param obj
     * @return
     */
    public static Map<String, Object> objToMap(Object obj) {
        return (Map<String, Object>) OBJECT_MAPPER.convertValue(obj, Map.class);
    }

    public static Map<String, String> objToStrMap(Object obj) {
        Map<String, Object> objMap = OBJECT_MAPPER.convertValue(obj, Map.class);
        if (objMap != null) {
            Map<String, String> strMap = new HashMap<String, String>(objMap.size(), 1);
            for (Map.Entry<String, Object> entry : objMap.entrySet()) {
                if (entry.getValue() != null) {
                    strMap.put(entry.getKey(), entry.getValue().toString());
                }
            }
            return strMap;
        }
        return null;
    }

    //字段value为null时，会把value改为""
    public static Map<String, String> objToStrMapWithoutNull(Object obj) {
        Map<String, Object> objMap = OBJECT_MAPPER.convertValue(obj, Map.class);
        if (objMap != null) {
            Map<String, String> strMap = new HashMap<String, String>(objMap.size(), 1);
            for (Map.Entry<String, Object> entry : objMap.entrySet()) {
                strMap.put(entry.getKey(), entry.getValue() == null? "":entry.getValue().toString());
            }
            return strMap;
        }
        return null;
    }

    //字段value为null时，不会把该字段转成map
    public static Map<String, String> objToStrMapWithoutNullSpec(Object obj) {
        Map<String, Object> objMap = OBJECT_MAPPER.convertValue(obj, Map.class);
        if (objMap != null) {
            Map<String, String> strMap = new HashMap<String, String>(objMap.size(), 1);
            for (Map.Entry<String, Object> entry : objMap.entrySet()) {
                if(entry.getValue() == null) {
                    continue;
                }
                strMap.put(entry.getKey(), entry.getValue().toString());
            }
            return strMap;
        }
        return null;
    }

    //字段value为null或""时，不会把该字段转成map
    public static Map<String, Object> objToStrMapWithoutEmpty(Object obj) {
        Map<String, Object> objMap = OBJECT_MAPPER.convertValue(obj, Map.class);
        if (objMap != null) {
            Map<String, Object> strMap = new HashMap<String, Object>(objMap.size(), 1);
            for (Map.Entry<String, Object> entry : objMap.entrySet()) {
                if (entry.getValue() == null || "".equals(entry.getValue().toString())) {
                    continue;
                }
                strMap.put(entry.getKey(), entry.getValue());
            }
            return strMap;
        }
        return null;
    }

    /**
     * 实体类转换为map,字段为NULl去掉
     *
     * @param obj
     * @return
     */
    public static Map<String, Object> objToNotNullMap(Object obj) {
        return (Map<String, Object>) NOT_NULL_MAPPER.convertValue(obj, Map.class);
    }

    public static String objToNotNullJson(Object obj) {
        try {
            return NOT_NULL_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warning("obj to not null json error" + e.getMessage());
        }
        return "";
    }

}
