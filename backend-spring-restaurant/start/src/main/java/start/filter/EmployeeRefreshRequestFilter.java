package start.filter;

import common.constant.JwtConstant;
import common.constant.RedisPrefixConstant;
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

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;



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
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);
        if (token == null) {
            // 没有 token，放行，由后面的认证过滤器决定是否 401
            filterChain.doFilter(request, response);
            return;
        }

        boolean shouldContinue = true; // 标记：是否继续放行到 Controller
        try {
            Map<String, Object> claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            if (claims == null) {
                shouldContinue = false;
                return;
            }
            String type = claims.get(JwtConstant.TYPE) != null ? claims.get(JwtConstant.TYPE).toString() : "user";
            if (!"emp".equals(type)) {
                // 非 emp 类型 token，直接放行（交给 user 的过滤器或后续逻辑）
                filterChain.doFilter(request, response);
                shouldContinue = false;
                return;
            }

            Long empId = Long.parseLong(claims.get(JwtConstant.EMP_ID).toString());
            String name = claims.get(JwtConstant.EMP_NAME) != null
                    ? claims.get(JwtConstant.EMP_NAME).toString() : "";

            String standardToken = stringRedisTemplate.opsForValue().get(RedisPrefixConstant.EMP_AUTHHEADER_PREFIX + empId);
            if (!token.equals(standardToken)) {
                log.error("emp Token 验证失败，已注销登录, 员工ID: {}", empId);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                shouldContinue = false;
                return;
            }

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    new LoginPrincipal(empId, name),
                    token,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 滑动过期
            stringRedisTemplate.expire(RedisPrefixConstant.EMP_AUTHHEADER_PREFIX + empId,
                    jwtProperties.getAdminTtl(), TimeUnit.SECONDS);

        } catch (ExpiredJwtException e) {
            // token 已过期 → 401，前端应引导重新登录
            log.warn("emp Token 已过期: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            shouldContinue = false;
        } catch (JwtException | IllegalArgumentException e) {
            // 仅处理 JWT 本身的异常（签名错/格式错/claims 缺字段）
            log.error("emp Token 非法: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            shouldContinue = false;
        }

        if (shouldContinue) {
            filterChain.doFilter(request, response);
        }
    }
}
