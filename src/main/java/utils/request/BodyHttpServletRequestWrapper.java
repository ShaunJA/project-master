package utils.request;

import utils.StreamUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Created by ShaunAJ on 2017/7/11.
 */
public class BodyHttpServletRequestWrapper extends  HttpServletRequestWrapper{
    private final byte[] body;
    private final Map<String, String[]> paramsMap = new HashMap<>();

    public BodyHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        body = StreamUtils.copyToByteArray(request.getInputStream());
        this.paramsMap.putAll(request.getParameterMap());
        if ("application/x-www-form-urlencoded".equals(getHeader("content-type"))) {
            String paramsStr = StreamUtils.copyToString(getInputStream(), Charset.forName("UTF-8")).replaceAll("\n|\r| ", "");
            if (null != paramsStr) {
                String[] params = paramsStr.split("&");
                for (int i = 0; i < params.length; i++) {
                    String[] p = params[i].split("=");
                    if (p.length == 2) {
                        paramsMap.put(p[0], new String[] { p[1] });
                    }
                }
            }
        }
    }



    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(paramsMap.keySet());
    }

    @Override
    public String getParameter(String name) {// 重写getParameter，代表参数从当前类中的map获取
        String[] values = paramsMap.get(name);
        if (values == null || values.length == 0) {
            return null;
        }
        return values[0];
    }

    @Override
    public String[] getParameterValues(String name) {// 同上
        return paramsMap.get(name);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {

        final ByteArrayInputStream bais = new ByteArrayInputStream(body);

        return new ServletInputStream() {

            @Override
            public int read() throws IOException {
                return bais.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }
        };
    }
}
