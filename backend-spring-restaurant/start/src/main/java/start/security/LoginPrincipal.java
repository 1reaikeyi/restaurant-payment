package start.security;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户认证主体信息
 * 存入 SecurityContext 的 principal，方便取 id 和 username
 */
@Data
@AllArgsConstructor
public class LoginPrincipal {
    private Long id;
    private String username;
}
/**
 *  LoginUserService.loadUserByUsername()  → new LoginUserDetails(user) 返回
 *           ↓
 *   DaoAuthenticationProvider 调 ud.getPassword() 比对密码、调 isEnabled() 查账户状态
 *           ↓
 *   认证成功后，整个 LoginUserDetails 作为 principal 放进 SecurityContext
 *           ↓
 *   Controller 里 (LoginUserDetails) SecurityContextHolder...getPrincipal() 取出当前用户
 */