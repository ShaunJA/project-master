package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by ShaungAJ on 2017/7/11.
 * 用于获取请求头中的设备信息
 */
public class IpUtil {
    final static Logger logger = LoggerFactory.getLogger(IpUtil.class);

    /**
     * 获取IP地址
     *
     * @param request HttpServletRequest
     * @return IP地址
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if ((ip == null) || (ip.length() == 0) || ("unknown".equalsIgnoreCase(ip))) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if ((ip == null) || (ip.length() == 0) || ("unknown".equalsIgnoreCase(ip))) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if ((ip == null) || (ip.length() == 0) || ("unknown".equalsIgnoreCase(ip))) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if ((ip == null) || (ip.length() == 0) || ("unknown".equalsIgnoreCase(ip))) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if ((ip == null) || (ip.length() == 0) || ("unknown".equalsIgnoreCase(ip))) {
            ip = request.getRemoteAddr();
        }

        // 处理代理IP的
        // 答案是取X-Forwarded-For中第一个非unknown的有效IP字符串。如：
        // X-Forwarded-For：192.168.1.110, 192.168.1.120, 192.168.1.130,
        // 192.168.1.100
        // 用户真实IP为： 192.168.1.110
        if (0 <= ",".indexOf(ip)) {
            return ip.substring(0, ip.indexOf(","));
        }

        return ip;
    }
}
