package utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by ShaunAJ on 2017/7/11.
 */
public class EntityUtils {
    // 格式化标识符
    public static final String format_separator_pattern = "\\|";
    public static final String format_separator_str = "|";

    // 别名标识符
    public static final String alias_separator_pattern = "\\:";
    public static final String alias_separator_str = ":";

    // 组合类标识符
    public static final String children_separator_pattern = "\\.";
    public static final String children_separator_str = ".";

    // 默认值标识符
    public static final String default_separator_pattern = "=";
    public static final String default_separator_str = "=";

    // 是否开启DEBUG模式
    public static final boolean DEBUG = false;

    // 时间类型
    @SuppressWarnings("rawtypes")
    public final static List dateTypes = Arrays.asList(java.util.Date.class, java.sql.Timestamp.class, java.sql.Date.class);

    // 数字类型
    @SuppressWarnings("rawtypes")
    public final static List numberTypes = Arrays.asList(Integer.class, Long.class, Float.class, Double.class, BigDecimal.class);

    // 集合类型数组
    @SuppressWarnings("rawtypes")
    public final static List basicCollections = Arrays.asList(List.class, Set.class);

    // 自动格式化的几种类型，其他类型不做处理
    public final static String dateType = "date";// 时间类型
    public final static String numberType = "number";// 数字类型

    /**
     * 获取类对象中所有的基本属性字段名称
     *
     * @param entityClass 类对象
     * @return {@link List}<{@link String}> 属性字段名称数组
     */
    public static List<String> getDeclaredFields(Class<?> entityClass) {
        List<String> fields = new ArrayList<>();
        // 检查是否有父类，如果继承了Object以外的其他父类，则获取父类的属性
        Class<?> superClass = entityClass.getSuperclass();
        if (superClass != null && (superClass != Object.class)) {
            fields.addAll(getDeclaredFields(entityClass.getSuperclass()));
        }
        // 获取类所有的列
        Field[] fs = entityClass.getDeclaredFields();
        // 便利列名称，并且添加到字符串数组中
        for (Field field : fs) {
            fields.add(field.getName());
        }

        return fields;
    }

    /**
     * 获取类对象中所有的基本属性字段名称,并去除部分字段
     *
     * @param entityClass 类对象
     * @return {@link List}<{@link String}> 属性字段名称数组
     */
    public static List<String> getDeclaredFields(Class<?> entityClass, String... removeFields) {
        List<String> fields = EntityUtils.getDeclaredFields(entityClass);
        fields.removeAll(Arrays.asList(removeFields));
        return fields;
    }

    /**
     * 根据传递的的keys参数来获取obj中所对应字段的值，转换为一个Map返回
     *
     * @param entity 已经实例化的对象
     * @param keys   需要获取的对象字段的数组
     * @return {@link Map}
     */
    public static Map<String, Object> getDeclaredParameters(Object entity, List<String> keys) {
        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, Object> children = new HashMap<String, Object>();

        printlnLog("开始遍历类中所有的字段");
        for (String key : keys) {
            // 去除格式化信息的KEY
            String basicKey = getBasicKey(key);
            String firstKey = getFirstKey(basicKey);

            // 如果有二级遍历，则保存二级遍历信息
            if (basicKey.contains(EntityUtils.children_separator_str)) {
                addChildrenKey(key, children);
            } else {
                try {
                    // 获取传递进来的对象类
                    Class<?> entityClass = entity.getClass();

                    String methodName = EntityUtils.getMethodName(firstKey);
                    Method method = entityClass.getMethod(methodName);
                    Object value = method.invoke(entity);
                    Class<?> type = method.getReturnType();

                    value = getValue(value, type, key);
                    String mapKey = getMapKey(key);

                    result.put(mapKey, value);

                } catch (NoSuchMethodException | SecurityException e) {
                    result.put(getMapKey(key), getDefaultValue(key));
                    // e.printStackTrace();
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    result.put(getMapKey(key), getDefaultValue(key));
                    // e.printStackTrace();
                }
            }

        }

        EntityUtils.getDeclaredChildrenParameters(result, children, entity);

        return result;
    }

    /**
     * 根据传递的的keys参数来获取objs数组中所对应字段的值，转换为一个Map返回
     *
     * @param collection 已经实例化的对象数组
     * @param keys       需要获取的对象字段的数组
     * @return {@link List}<{@link Map}<{@link String},{@link Object}>>
     */
    public static List<Map<String, Object>> getListDeclaredParameters(Collection<?> collection, List<String> keys) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (null != collection && 0 < collection.size()) {
            result = new ArrayList<Map<String, Object>>();
            for (Object obj : collection) {
                result.add(EntityUtils.getDeclaredParameters(obj, keys));
            }
        }

        return result;
    }

    /**
     * 获取二级或二级以下字段的值，转换为一个Map返回
     *
     * @param result      一级的返回值
     * @param children    子类的key
     * @param superEntity 主类的类型
     */
    private static void getDeclaredChildrenParameters(Map<String, Object> result, Map<String, Object> children, Object superEntity) {
        printlnLog("children:" + children.keySet());
        Set<String> keySet = children.keySet();

        // 获取传递进来的对象类
        Class<?> entityClass = superEntity.getClass();

        for (String childrenKey : keySet) {
            // 去除格式化信息的KEY
            String basicKey = getBasicKey(childrenKey);
            String firstKey = getFirstKey(basicKey);
            try {
                String methodName = getMethodName(firstKey);
                Method method = entityClass.getMethod(methodName);
                Object subObject = method.invoke(superEntity);
                if (null != subObject) {
                    // 获取子分支所需要获取的字段名称数组
                    @SuppressWarnings("unchecked")
                    List<String> subKeys = (ArrayList<String>) children.get(childrenKey);
                    // 如果子元素是数组类型，则反射数组中的信息
                    if (EntityUtils.basicCollections.contains(method.getReturnType())) {
                        result.put(childrenKey, getListDeclaredParameters((Collection<?>) subObject, subKeys));
                    } else {
                        result.put(childrenKey, getDeclaredParameters(subObject, subKeys));
                    }
                } else {
                    result.put(childrenKey, null);
                }
            } catch (NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 如果字段使用了别名，这里会直接返回别名，如果没有则返回正常字段名称
     *
     * @param key
     * @return
     */
    private static String getMapKey(String key) {
        String basicKey = getBasicKey(key);
        printlnLog("获取字段的名称[key=" + key + "]");

        if (basicKey.contains(EntityUtils.default_separator_str)) {
            basicKey = key.split(EntityUtils.default_separator_pattern)[0].trim();
        }

        if (basicKey.contains(EntityUtils.alias_separator_str)) {
            basicKey = basicKey.split(EntityUtils.alias_separator_pattern)[1].trim();
        }

        return basicKey;
    }

    private static String getDefaultValue(String key) {
        if (key.contains(EntityUtils.default_separator_str)) {
            return key.split(EntityUtils.default_separator_pattern)[1].trim();
        }

        return null;
    }

    /**
     * 获取值，在这里面会自动进行格式化和转换等操作
     *
     * @param value 需要格式化的值
     * @param type  数据类型
     * @param key   字段名称
     * @return
     */
    public static Object getValue(Object value, Class<?> type, String key) {
        printlnLog("获取字段值，这里会做自动化处理[value=" + value + ",type=" + type + ",key=" + key + "]");
        if (null != value) {
            if (EntityUtils.needPattern(key)) {
                return format(value, key);
            } else if (dateTypes.contains(type)) {
                return ((Date) value).getTime();
            }
        }
        return value;
    }

    /**
     * 格式化值
     *
     * @param value 需要格式化的值
     * @param key   需要格式化的字段名称
     * @return
     */
    private static Object format(Object value, String key) {
        String pattern = getPattern(key);
        String formatType = getObjectType(value);
        printlnLog("格式化值[pattern:" + pattern + ",formatType:" + formatType + "]");

        return format(value, pattern, formatType);
    }

    /**
     * 格式化值
     *
     * @param value      需要格式化的值
     * @param pattern    格式化模式
     * @param formatType 格式化类型
     * @return
     */
    private static Object format(Object value, String pattern, String formatType) {
        switch (formatType) {
            case EntityUtils.dateType: {
                return dateFormat(value, pattern);
            }
            case EntityUtils.numberType: {
                return numberFormat(value, pattern);
            }
            default:
                return value;
        }
    }

    /**
     * 日期类型格式化
     *
     * @param object  格式化的对象
     * @param pattern 格式的模式字符串
     * @return 完成格式化的信息
     */
    private static String dateFormat(Object object, String pattern) {
        printlnLog("dateFormat:[object=" + object + ",pattern=" + pattern + "]");
        if (object == null) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(object);
    }

    /**
     * 数字类型格式化
     *
     * @param object  格式化的对象
     * @param pattern 格式的模式字符串
     * @return 完成格式化的信息
     */
    private static String numberFormat(Object object, String pattern) {
        printlnLog("numberFormat:[object=" + object + ",pattern=" + pattern + "]");
        if (object == null) {
            return null;
        }
        DecimalFormat formatter = new DecimalFormat(pattern);
        return formatter.format(object);
    }

    /**
     * 获取需要格式化的字段的数据类型
     *
     * @param object
     * @return
     */
    private static String getObjectType(Object object) {
        printlnLog("获取当前字段可以自动格式化的类型[object=" + object + "]");
        if (object == null) {
            throw new RuntimeException("The object can not be null");
        }

        if (EntityUtils.numberTypes.contains(object.getClass())) {
            return EntityUtils.numberType;
        }

        if (EntityUtils.dateTypes.contains(object.getClass())) {
            return EntityUtils.dateType;
        }

        throw new RuntimeException("只支持数字与实践类型的格式化");
    }

    /**
     * 获取格式化模式字符串
     *
     * @param key 需要格式化的字段名称
     * @return
     */
    private static String getPattern(String key) {
        printlnLog("获取格式化字符串[key=" + key + "]");
        return key.split(EntityUtils.format_separator_pattern)[1].trim();
    }

    /**
     * 判断是否需要格式化
     *
     * @param key
     * @return
     */
    private static boolean needPattern(String key) {
        printlnLog("判断是否需要格式化[key=" + key + "]");
        String basicKey = EntityUtils.getBasicKey(key);
        if (basicKey.contains(EntityUtils.children_separator_str)) {
            return false;
        }

        return key.contains(EntityUtils.format_separator_str);
    }

    /**
     * 根据传递进来的KEY来判断是否存在格式化相关分隔符<br>
     * 如果存在则去除分隔符之后的东西,自动去除拆分后的前后空格<br>
     * 例：传递进来的key="createTime | yyyy-MM-dd" ,返回的内容为"createTime"
     *
     * @param key
     * @return
     */
    public static String getBasicKey(String key) {
        printlnLog("获取基本字段名称,去除了格式化信息[key=" + key + "]");
        // 判断是否存在格式化标识符
        if (key.contains(EntityUtils.format_separator_str)) {
            return key.split(EntityUtils.format_separator_pattern)[0].trim();
        }
        return key;
    }

    /**
     * 获取key中的第一级名称<br>
     * 例：传递进来的key="user.name"，返回的内容为"user"
     *
     * @param key
     * @return
     */
    public static String getFirstKey(String key) {
        printlnLog("获取当前一级类字段名称[key=" + key + "]");
        // 判断是否存在二级
        if (key.contains(EntityUtils.children_separator_str)) {
            String firstKey = key.split(EntityUtils.children_separator_pattern)[0].trim();
            return removeAlias(firstKey);
        }

        return removeAlias(key);
    }

    /**
     * 去除别名
     *
     * @param key
     * @return
     */
    public static String removeAlias(String key) {
        printlnLog("去除别名[key=" + key + "]");
        if (key.contains(EntityUtils.alias_separator_str)) {
            return key.substring(0, key.indexOf(EntityUtils.alias_separator_str)).trim();
        }
        return key;
    }

    /**
     * 添加二级的KEY
     *
     * @param key
     * @param children
     */
    public static void addChildrenKey(String key, Map<String, Object> children) {
        String firstKey = getFirstKey(key);
        @SuppressWarnings("unchecked")
        List<String> childrenKeyList = (ArrayList<String>) children.get(firstKey);
        if (null != childrenKeyList) {
            childrenKeyList.add(key.replaceFirst(firstKey.concat("."), ""));
        } else {
            childrenKeyList = new ArrayList<>();
            childrenKeyList.add(key.replaceFirst(firstKey.concat("."), ""));
            children.put(firstKey, childrenKeyList);
        }
    }

    /**
     * 根据字段名称获取该字段的get方法
     *
     * @param key 字段名称
     * @return
     */
    public static String getMethodName(String key) {
        if (null == key || "".equals(key) || 0 > key.length()) {
            return null;
        }

        char[] buf = key.toCharArray();
        buf[0] = Character.toUpperCase(buf[0]);
        return "get".concat(String.valueOf(buf));
    }

    /**
     * 打印信息
     *
     * @param message
     */
    private static void printlnLog(String message) {
        if (EntityUtils.DEBUG) {
            System.out.println(message);
        }
    }
}
