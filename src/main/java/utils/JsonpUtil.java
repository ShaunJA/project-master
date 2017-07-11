package utils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JSONP返回工具
 * Created by ShaunAJ on 2017/7/11.
 */
public class JsonpUtil {
    public static void jsonpResult(String jsonpCallback, Object result, HttpServletResponse response) {
        response.setContentType("text/plain;charset=utf-8");
        try {
            response.getWriter().write(jsonpCallback + "([" + JsonUtils.toJSONString(result) + "])"); // 返回jsonp数据
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
