package cn.lizongyi.shareaccount.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Component
public class LoggingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化代码（如果需要）
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String userId = httpRequest.getHeader("Additional");
        String userIp = httpRequest.getRemoteAddr(); // 或者使用自定义方法获取 IP 地址
        String httpMethod = httpRequest.getMethod();
        String url = httpRequest.getRequestURI();
        String traceId = UUID.randomUUID().toString().replace("-", "");

        MDC.put("userId", userId);
        MDC.put("traceId", traceId);
        MDC.put("userIp", userIp);
        MDC.put("httpMethod", httpMethod);
        MDC.put("url", url);

        long startTime = System.currentTimeMillis();

        // 缓存请求内容
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpResponse);

        log.info("--------------------接口开始---------------");

        try {
            // 记录请求参数
            if ("GET".equalsIgnoreCase(httpMethod) || "DELETE".equalsIgnoreCase(httpMethod)) {
                // 对于 GET 请求，直接从查询字符串中获取参数
                Enumeration<String> parameterNames = httpRequest.getParameterNames();
                String params = StreamSupport.stream(((Iterable<String>) () -> parameterNames.asIterator()).spliterator(), false)
                                             .map(paramName -> paramName + "=" + httpRequest.getParameter(paramName))
                                             .collect(Collectors.joining(", "));
                log.info("Request Parameters: {}", params);
            } else {
                // 对于非 GET 请求（例如 POST），从请求体中获取参数
                byte[] content = wrappedRequest.getContentAsByteArray();
                if (content.length > 0) {
                    String requestBody = new String(content, StandardCharsets.UTF_8);
                    log.info("Request Body: {}", requestBody);
                }
            }

            chain.doFilter(wrappedRequest, wrappedResponse);

            int responseStatus = wrappedResponse.getStatus();
            MDC.put("responseStatus", String.valueOf(responseStatus));

            // 获取响应体内容
            byte[] content = wrappedResponse.getContentAsByteArray();
            String responseContent = null;
            if (content.length > 0) {
                responseContent = new String(content, StandardCharsets.UTF_8);
                log.info("Response Body: {}", responseContent);
            }
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            MDC.put("duration", String.valueOf(duration));
            wrappedResponse.copyBodyToResponse();
            log.info("--------------------接口结束---------------");
            MDC.clear(); // 清除 MDC 中的数据，确保线程安全
        }
    }

    @Override
    public void destroy() {
        // 销毁代码（如果需要）
    }
}