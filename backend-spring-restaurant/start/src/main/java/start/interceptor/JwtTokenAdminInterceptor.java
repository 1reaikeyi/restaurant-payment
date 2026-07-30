package start.interceptor;

import common.constant.JwtClaimsConstant;
import common.localContextHolder.ThreadLocalContextHolder;
import common.properties.JwtProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import common.utils.JwtUtil;
import org.springframework.web.servlet.ModelAndView;


import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String KEYS = "emp:";
    /**
     * 校验jwt
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }
        //1、从请求头中获取令牌
        String token = request.getHeader("Authorization");
        try {
            if (token == null) {
                // 没有登录，返回错误信息
                response.setStatus(401);
                return false;
            }
        //2、校验令牌
            Map<String, Object> claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            String currentId = claims.get(JwtClaimsConstant.EMP_ID).toString();
            Long id = Long.parseLong(currentId);
            String standard_token = stringRedisTemplate.opsForValue().get(KEYS + id);
            if (!standard_token.equals(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            // 刷新 Redis 中 token 的过期时间（滑动过期策略）
            stringRedisTemplate.expire(KEYS + id, jwtProperties.getAdminTtl(), TimeUnit.SECONDS);
            // 将用户id设置到ThreadContext中
            ThreadLocalContextHolder.set(claims);
            return true;
        } catch (Exception e) {
            //4、不通过，响应401状态码
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(e.getMessage());
            return false;
        }

    }
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        ThreadLocalContextHolder.remove();
    }
}
