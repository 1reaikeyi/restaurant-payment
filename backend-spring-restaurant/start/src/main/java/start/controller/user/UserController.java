package start.controller.user;

import cn.hutool.core.bean.BeanUtil;
import common.constant.*;
import common.properties.JwtProperties;
import common.result.Result;
import common.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import model.dto.EmployeeDTO;
import model.dto.LoginDTO;
import model.dto.UserDTO;
import model.entity.Employee;
import model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.UserService;
import start.security.LoginPrincipal;
import start.security.SecurityContextParam;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private PasswordEncoder passwordEncoder; // 注入密码加密器 Bean
    @Autowired
    private AuthenticationManager authenticationManager; // 注入认证管理器

    @PostMapping("/register")
    public Result register(@RequestBody UserDTO userDTO) {
        User validUser = userService.findUsername(userDTO.getUsername());
        if (validUser != null) {
            return Result.error(ErrorConstant.USERNAME_EXIST);
        }
        User user= BeanUtil.toBean(userDTO, User.class);
        user.setStatus(StatusConstant.ENABLE);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.save(user);
        return Result.success("register::" + user.getId());
    }
    @PostMapping("/login")
    public Result login(@RequestBody LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                "user:" + username, password);
        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        if (!authentication.isAuthenticated()){
            return Result.error(ErrorConstant.PASSWORD_ERROR);
        }
        // 认证成功后，查询用户完整信息
        User user = userService.findUsername(username);
        Map<String,Object> map = new HashMap<>();
        map.put(JwtConstant.USER_ID, user.getId());
        map.put(JwtConstant.USER_NAME, user.getUsername());
        map.put(JwtConstant.TYPE, "user"); // type 必须与 UserRefreshRequestFilter 校验的 "user" 一致，否则过滤器不识别该 token 导致 401
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                new LoginPrincipal(user.getId(), user.getUsername()),
                null,
                Collections.singletonList(new SimpleGrantedAuthority(RoleConstant.ROLE_ADMIN))
        );
        // 构建包含用户ID的认证对象并设置到 SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        String token = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getUserTtl(), map);
        stringRedisTemplate.opsForValue().set(RedisPrefixConstant.USER_AUTHHEADER_PREFIX+ user.getId(), token,
                jwtProperties.getUserTtl(), TimeUnit.SECONDS);
        return Result.success(token);

    }
    @PostMapping("/logout")
    public Result logout() {
        // 获取当前登录用户ID
        Long userId = SecurityContextParam.getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }
        //删除
        stringRedisTemplate.delete(RoleConstant.ROLE_USER+ userId);
        //删除线程
        SecurityContextHolder.clearContext();
        return Result.success("logout");
    }

}
