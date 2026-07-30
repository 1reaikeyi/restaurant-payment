package start.controller.user;

import cn.hutool.core.bean.BeanUtil;
import common.constant.ErrorConstant;
import common.constant.JwtClaimsConstant;
import common.localContextHolder.ThreadLocalContextHolder;
import common.properties.JwtProperties;
import common.result.Result;
import common.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pojo.dto.user.UserDTO;
import pojo.dto.user.UserLogInDTO;
import pojo.entity.User;
import service.ISevcive.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user/users")
@Transactional(rollbackFor = Exception.class)
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private static final String KEY_PREFIX = "user:";
    @PostMapping("/register")
    public Result register(@RequestBody UserDTO userDTO) {
        // 检查用户名是否已存在
        User checkUser = userService.loadByUsername(userDTO.getUserName());
        if (checkUser != null) {
            return Result.error(ErrorConstant.USERNAME_EXIST);
        }
        userDTO.setOpenId(DigestUtils.md5DigestAsHex(userDTO.getOpenId().getBytes()));
        User user = BeanUtil.toBean(userDTO, User.class);
        userService.save(user);
        return Result.success("/register::"+user.getId());
    }
    @PostMapping("/login")
    public Result login(@RequestBody UserLogInDTO userLogInDTO) {
        User user = userService.login(userLogInDTO);
        if (user == null) {
            return Result .error(ErrorConstant.PASSWORD_ERROR);
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        claims.put(JwtClaimsConstant.USERNAME, user.getUserName());
        ThreadLocalContextHolder.set(claims);
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        stringRedisTemplate.opsForValue().set(KEY_PREFIX + user.getId(),token,jwtProperties.getUserTtl(), TimeUnit.SECONDS);
        return Result.success(user.getId() + "::" + token);
    }
    @PostMapping("/logout")
    public Result logout() {
        Map<String,Object> claims = ThreadLocalContextHolder.get();
        String currentId = claims.get(JwtClaimsConstant.EMP_ID).toString();
        Long empId = Long.valueOf(currentId);
        stringRedisTemplate.delete(KEY_PREFIX +empId);
        ThreadLocalContextHolder.remove();
        return Result.success("logout::"+empId);
    }
}