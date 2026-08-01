package start.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
//test
@RestController
@EnableMethodSecurity(prePostEnabled = true)
@RequestMapping("/test")
public class LoginRole {
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @RequestMapping("/admin")
    public String select() {
        return "hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')";
    }
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    @RequestMapping("/user")
    public String delete() {
        return "hasRole('ROLE_ADMIN')";
    }
}