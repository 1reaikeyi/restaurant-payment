package start.filter;

import common.constant.JwtConstant;
import common.properties.JwtProperties;
import common.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import start.security.LoginPrincipal;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static common.constant.RedisPrefixConstant.RESTAURANT_EMP_AUTHHEADER_PREFIX;


/**
 * emp 员工 Token 刷新与验证过滤器
 *
 * 职责：
 * 1. 提取请求头中的 Token
 * 2. 只处理 emp 类型（TYPE=emp）的 Token，user 的 Token 直接放行
 * 3. 用 Redis 校验（weibo:emp:{id}）并刷新过期时间
 * 4. 设置 SecurityContext（ROLE_ADMIN）
 */
@Slf4j
public class EmployeeRefreshRequestFilter extends OncePerRequestFilter {

    private final JwtProperties jwtProperties;
    private final StringRedisTemplate stringRedisTemplate;

    public EmployeeRefreshRequestFilter(JwtProperties jwtProperties, StringRedisTemplate stringRedisTemplate) {
        this.jwtProperties = jwtProperties;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String token = extractToken(request);
        if (token == null) {
            // 没有 token，放行，由后面的认证过滤器决定是否 401
            filterChain.doFilter(request, response);
            return;
        }
        try {
            Map<String, Object> claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            if (claims == null) {
                return;
            }
            String type = claims.get(JwtConstant.TYPE) != null ? claims.get(JwtConstant.TYPE).toString() : "user";
            if (!"emp".equals(type)) {
                filterChain.doFilter(request, response);
                return;
            }

            Long empId = Long.parseLong(claims.get(JwtConstant.EMP_ID).toString());
            String name = claims.get(JwtConstant.EMP_NAME) != null
                    ? claims.get(JwtConstant.EMP_NAME).toString() : "";

            String standardToken = stringRedisTemplate.opsForValue().get(RESTAURANT_EMP_AUTHHEADER_PREFIX + empId);
            if (!token.equals(standardToken)) {
                log.error("emp Token 验证失败，可能已注销或被篡改, 员工ID: {}", empId);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    new LoginPrincipal(empId, name),
                    token,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 滑动过期
            stringRedisTemplate.expire(RESTAURANT_EMP_AUTHHEADER_PREFIX + empId,
                    jwtProperties.getAdminTtl(), TimeUnit.SECONDS);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("emp JWT 处理失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
