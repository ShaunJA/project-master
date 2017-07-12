package utils.ssdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.JsonUtils;
import utils.StringUtils;

import java.util.List;

/**
 * Created by ShaunAJ on 2017/7/11.
 */
public class SSDBCacheUtil {
    private final static Logger logger = LoggerFactory.getLogger(SSDBCacheUtil.class);

    public static void remove(String key) {
        SSDB ssdb = SSDB.getInstance();
        try {
            if (!StringUtils.isEmpty(key)) {
                ssdb.request("del", key);
            }
        } catch (Exception e) {
            logger.info("context", e);
        } finally {
            if (null != ssdb) {
                ssdb.close();
            }
        }
    }

    public static void addHset(String name, String key, String val) {
        SSDB ssdb = SSDB.getInstance();
        try {
            ssdb.hset(name, key, val);
        } catch (Exception e) {
            logger.info("context", e);
        } finally {
            if (null != ssdb) {
                ssdb.close();
            }
        }
    }

    public static <T> T getHset(String name, String key, Class<T> objClass) {
        SSDB ssdb = SSDB.getInstance();
        try {
            byte[] hget = ssdb.hget(name, key);
            if (null != hget) {
                return JsonUtils.toObject(new String(hget, "utf-8"), objClass);
            }
        } catch (Exception e) {
            logger.info("context", e);
            try {
                ssdb.hdel(name, key);
            } catch (Exception e1) {
                logger.info("context", e1);
            }
        } finally {
            if (null != ssdb) {
                ssdb.close();
            }
        }
        return null;
    }

    public static <T> List<T> getHsetList(String name, String key, Class<T> objClass) {
        SSDB ssdb = SSDB.getInstance();
        try {
            byte[] hget = ssdb.hget(name, key);
            if (null != hget) {
                return JsonUtils.toList(new String(hget, "utf-8"), objClass);
            }
        } catch (Exception e) {
            logger.info("context", e);
            try {
                ssdb.hdel(name, key);
            } catch (Exception e1) {
                logger.info("context", e1);
            }
        } finally {
            if (null != ssdb) {
                ssdb.close();
            }
        }
        return null;
    }

    /**
     * 保存的接口内容到SSDB
     *
     * @param key 保存的KEY
     * @param obj 保存的值
     * @param sec 存活时间(秒)
     */
    public static void add(String key, Object obj, int sec) {
        if (StringUtils.isEmpty(key) || null == obj || 0 >= sec) {
            return;
        }
        SSDB ssdb = SSDB.getInstance();
        try {
            ssdb.request("setx", key, JsonUtils.toJSONString(obj), String.valueOf(sec));
        } catch (Exception e) {
            logger.info("context", e);
        } finally {
            if (null != ssdb) {
                ssdb.close();
            }
        }
    }

    public static <T> T get(String key, Class<T> objClass) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        SSDB ssdb = SSDB.getInstance();
        try {
            byte[] bs = ssdb.get(key);
            if (null != bs) {
                return JsonUtils.toObject(new String(bs, "utf-8"), objClass);
            }
        } catch (Exception e) {
            logger.info("context", e);
            try {
                ssdb.del(key);
            } catch (Exception e1) {
                logger.info("context", e1);
            }
        } finally {
            if (null != ssdb) {
                ssdb.close();
            }
        }
        return null;
    }

    public static <T> List<T> getList(String key, Class<T> objClass) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        SSDB ssdb = SSDB.getInstance();
        try {
            byte[] bs = ssdb.get(key);
            // ssdb.del(key);
            if (null != bs) {
                return JsonUtils.toList(new String(bs, "utf-8"), objClass);
            }

        } catch (Exception e) {
            logger.info("context", e);
            try {
                ssdb.del(key);
            } catch (Exception e1) {
                logger.info("context", e1);
            }
        } finally {
            if (null != ssdb) {
                ssdb.close();
            }
        }
        return null;
    }
}
