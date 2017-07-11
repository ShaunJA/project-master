package utils.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.StreamUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ShaunAJ on 2017/7/11.
 */
public class RequestLogFilter  implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(RequestLogFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ServletRequest requestWrapper = new BodyHttpServletRequestWrapper((HttpServletRequest) request);
        Map<String, String> map = new HashMap<>();
        Enumeration<String> paramNames = requestWrapper.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();

            String[] paramValues = requestWrapper.getParameterValues(paramName);
            if (paramValues.length == 1) {
                String paramValue = paramValues[0];
                if (paramValue.length() != 0) {
                    map.put(paramName, paramValue);
                }
            }
        }
        logger.info("Request     :[" + ((HttpServletRequest) request).getMethod() + "] " + ((HttpServletRequest) request).getRequestURL());
        logger.info("Parameter   :" + map);
        logger.info("InputStream :" + StreamUtils.copyToString(requestWrapper.getInputStream(), Charset.forName("UTF-8")).replaceAll("\n|\r| ", ""));
		/* 打印头信息 */
        java.util.Enumeration<String> names = ((HttpServletRequest) request).getHeaderNames();
        logger.info("===================================================================");
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            logger.info(name + ":" + ((HttpServletRequest) request).getHeader(name));
        }
        logger.info("===================================================================");
        chain.doFilter(requestWrapper, response);

    }

    @Override
    public void destroy() {
    }
}
