package io.github.qyvlik.springhmacrestverify.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class ServletUtils {

    public static HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
            if (attributes == null) {
                return null;
            }
            return attributes.getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getIpFromRequest(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            int index = ip.indexOf(",");
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.isNotEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            return ip;
        }

        return request.getRemoteAddr();
    }

    public static String getIp() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            return getIpFromRequest(request);
        }

        return null;
    }

    public static void writeJsonString(HttpServletResponse response, String jsonString, int status) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.setStatus(status);
        PrintWriter writer = response.getWriter();
        writer.append(jsonString);
    }

}
