package start.security;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Spring Security 用户信息工具类
 *
 */
public class SecurityContextParam {
    /**
     * 获取当前登录用户主体信息
     *
     * @return LoginPrincipal，如果未登录返回null
     */
    public static LoginPrincipal getLoginPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof LoginPrincipal) {
            return (LoginPrincipal) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前登录用户的ID
     *
     * @return 用户ID，如果未登录返回null
     */
    public static Long getCurrentUserId() {
        LoginPrincipal principal = getLoginPrincipal();
        return principal != null ? principal.getId() : null;
    }

    /**
     * 获取当前登录用户名
     *
     * @return 用户名，如果未登录返回null
     */
    public static String getCurrentUsername() {
        LoginPrincipal principal = getLoginPrincipal();
        return principal != null ? principal.getUsername() : null;
    }


}