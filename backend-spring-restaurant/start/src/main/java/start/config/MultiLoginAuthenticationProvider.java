package start.config;

import model.entity.Employee;
import model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import service.EmployeeService;
import service.UserService;
import start.security.LoginPrincipal;

import java.util.Collections;

@Component
public class MultiLoginAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserService userService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String principal = authentication.getName();
        String password = (String) authentication.getCredentials();

        int idx = principal.indexOf(':');
        String type = idx > 0 ? principal.substring(0, idx) : "user";
        String username = idx > 0 ? principal.substring(idx + 1) : principal;
        // ===== emp 员工登录=====
        if ("emp".equals(type)) {
            Employee employee = employeeService.findEmployeename(username);
            if (employee == null || !passwordEncoder.matches(password, employee.getPassword())) {
                throw new BadCredentialsException("用户名或密码错误");
            }
            return new UsernamePasswordAuthenticationToken(
                    new LoginPrincipal(employee.getId(), employee.getUsername(),"emp"),
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        }

        // ===== user 普通用户登录=====
        User user = userService.findUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("用户名或密码错误");
        }
        return new UsernamePasswordAuthenticationToken(
                new LoginPrincipal(user.getId(), user.getUsername(),"user"),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
