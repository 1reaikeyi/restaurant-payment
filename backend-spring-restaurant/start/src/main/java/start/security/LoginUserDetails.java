package start.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class LoginUserDetails implements UserDetails {
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return "";
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
/**

 * public class LoginUserDetails implements UserDetails {
 *       private final User user;   // 包装你自己的实体
 *
 *       public LoginUserDetails(User user) {
 *           this.user = user;
 *       }
 *
 *       public Long getUserId() { return user.getId(); }   // 顺手暴露，方便取 id
 *
 *       @Override
 *       public Collection<? extends GrantedAuthority> getAuthorities() {
 *           return List.of(new SimpleGrantedAuthority("ROLE_USER"));
 *       }
 *
 *       @Override
 *       public String getPassword() {
 *           return user.getPassword();    // 框架拿它跟输入的密码做 matches 比对
 *       }
 *
 *       @Override
 *       public String getUsername() {
 *           return user.getUsername();
 *       }
 *
 *       // 四个账户状态，返回 false 会让认证直接失败
 *       @Override
 *       public boolean isEnabled()               { return true; }            // 常见写法：user.getStatus() == 1
 *       @Override
 *       public boolean isAccountNonExpired()     { return true; }
 *       @Override
 *       public boolean isAccountNonLocked()      { return true; }
 *       @Override
 *       public boolean isCredentialsNonExpired() { return true; }
 *   }
 */