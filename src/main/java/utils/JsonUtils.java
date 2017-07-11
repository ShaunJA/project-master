package utils;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.sun.org.apache.xml.internal.utils.StringToStringTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * JSON工具类，需要依赖jackson-databind
 * Created by ShaunAJ on 2017/7/11.
 */
public class JsonUtils {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    private static final JsonNodeFactory factory = new JsonNodeFactory(false);
    private static final ObjectMapper objectMapper = new ObjectMapper();


    private JsonUtils() {

    }

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 把list对象转换JSON为字符串
     * @param list 需要转换为json字符串的数组
     * @param <T> 数组对应类型的泛型
     * @return list对应的json字符串
     */
    public static <T> String toJSONString(List<T> list) {
        try {
            //用objectMapper直接返回list转换成的JSON字符串
            return objectMapper.writeValueAsString(list);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 把Object对象转换为JSON字符串
     * @param object 需要转换为json字符串的对象
     * @return object对应的json字符串
     */
    public static String toJSONString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * object对象
     * @param object
     * @return
     */
    public static Map toHashMap(String object) {
        Map<String, Object> data = new HashMap<>();
        try {
            data = objectMapper.readValue(object, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    /**
     * 读取JSON字符串转换为指定对象类型
     * @param jsonStr json字符串
     * @param type 目标类型
     * @param <T> 目标类型泛型
     * @return 目标类型数组
     */
    public static <T> List<T> toList(String jsonStr, Class<T> type) {
        if (null != jsonStr && !"".equals(jsonStr)) {
            try {
                return objectMapper.readValue(jsonStr, objectMapper.getTypeFactory().constructParametricType(List.class, type));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return Collections.emptyList();
    }

    public static <T> T toObject(String jsonStr, Class<T> type) {
        if (null != jsonStr) {
            try {
                return objectMapper.readValue(jsonStr, type);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    public static void main(String[] args) {
        List<String> list = new ArrayList<String>();
        list.add("123");
        System.out.println(JsonUtils.toHashMap("{\"key\":\"123\"}"));
        System.out.println(JsonUtils.toJSONString("as3a54sd"));;
    }
}
