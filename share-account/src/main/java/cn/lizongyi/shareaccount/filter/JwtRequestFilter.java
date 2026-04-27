package cn.lizongyi.shareaccount.filter;

import cn.lizongyi.shareaccount.util.IpUtil;
import cn.lizongyi.shareaccount.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private IpUtil ipUtil;

    // 定义不需要 JWT 验证的路径
    private final List<String> noCheckPaths = Arrays.asList(
            "/login/login",
            "/login/sms",
            "/api/pay/notify",
            "/slide/findOpenAll",
            "/goods/homelist",
            "/users/checkNewUser",
            "/login/guest",
            "/tool/syncIconfont",
            "/sms/send",
            "/config/theme",
            "/config/share"
    );

    // 定义需要正则匹配的路径
    private final Pattern noCheckPathPattern = Pattern.compile("^/goods/findById/\\d+$");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 获取请求的 URI
        String uri = request.getRequestURI();

        // 如果请求路径在不需要检查的路径列表中，则直接放行
        if (noCheckPaths.stream().anyMatch(uri::endsWith)) {
            chain.doFilter(request, response);
            return;
        }

        // 如果请求路径匹配正则表达式，则直接放行
        if (noCheckPathPattern.matcher(uri).matches()) {
            chain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Aboluo");
        String userId = request.getHeader("Additional");
        String userIp = ipUtil.getIpAddr(request);

        if (!StringUtils.hasText(userId) || "0".equals(userId)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "操作失败");
            return;
        }
        request.setAttribute("userId", Long.parseLong(userId));
        request.setAttribute("userIp", userIp);

        if (authorizationHeader != null && authorizationHeader.startsWith("Aboluo ")) {
            String jwt = authorizationHeader.substring(7);
            try {
                if (!jwtUtil.validateToken(jwt)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "操作失败");
                    return;
                }

                // 假设你需要根据 openid 加载用户信息并设置到 SecurityContext 中
                // 可以在这里进行相应的处理

            } catch (Exception e) {
                // 如果解析失败，可能是无效的 token
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "操作失败");
                return;
            }
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "操作失败");
            return;
        }

        chain.doFilter(request, response);
    }
}