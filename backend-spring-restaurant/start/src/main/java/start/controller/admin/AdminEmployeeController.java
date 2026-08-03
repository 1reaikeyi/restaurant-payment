package start.controller.admin;




import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.constant.*;
import common.enumOperation.OperationEnum;
import model.dto.EmployeePageDTO;
import org.springframework.transaction.annotation.Transactional;
import start.aop.OperationLogging;
import common.properties.JwtProperties;
import common.result.Result;
import common.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import model.dto.EmployeeDTO;
import model.dto.LoginDTO;
import model.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import service.EmployeeService;
import start.security.LoginPrincipal;
import start.security.SecurityContextParam;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class AdminEmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private PasswordEncoder passwordEncoder; // 注入密码加密器 Bean
    @Autowired
    private AuthenticationManager authenticationManager; // 注入认证管理器

    @PostMapping("/register")
    public Result register(@RequestBody EmployeeDTO employeeDTO) {
        Employee validEmployee = employeeService.findEmployeename(employeeDTO.getUsername());
        if (validEmployee != null) {
            return Result.error(ErrorConstant.USERNAME_EXIST);
        }
        Employee employee = BeanUtil.toBean(employeeDTO, Employee.class);
        employee.setStatus(StatusConstant.ENABLE);
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        employee.setCreateUser(0L);
        employee.setCreateUser(0L);
        employeeService.save(employee);

        return Result.success("register::" + employee.getId());
    }
    @PostMapping("/login")
    public Result login(@RequestBody LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                "emp:" + username, password);
        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        if (!authentication.isAuthenticated()){
            return Result.error(ErrorConstant.PASSWORD_ERROR);
        }
        // 认证成功后，查询用户完整信息
        Employee employee = employeeService.findEmployeename(username);
        Map<String,Object> map = new HashMap<>();
        map.put(JwtConstant.EMP_ID, employee.getId());
        map.put(JwtConstant.EMP_NAME, employee.getUsername());
        map.put(JwtConstant.TYPE, "emp"); // type 必须与 EmployeeRefreshRequestFilter 校验的 "emp" 一致，否则过滤器不识别该 token 导致 401
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                new LoginPrincipal(employee.getId(), employee.getUsername(),"emp"),
                null,
                Collections.singletonList(new SimpleGrantedAuthority(RoleConstant.ROLE_ADMIN))
        );
        // 构建包含用户ID的认证对象并设置到 SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        String token = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), map);
        stringRedisTemplate.opsForValue().set(RedisPrefixConstant.EMP_AUTHHEADER_PREFIX+ employee.getId(), token, jwtProperties.getAdminTtl(), TimeUnit.SECONDS);
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
        stringRedisTemplate.delete(RoleConstant.ROLE_ADMIN+ userId);
        //删除线程
        SecurityContextHolder.clearContext();
        return Result.success("logout");
    }
    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping
    public Result read(@RequestParam Long id) {
        return Result.success(employeeService.getById(id));
    }
    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/all")
    public Result readPage(EmployeePageDTO employeePageDTO) {
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getStatus, StatusConstant.ENABLE)
                .like(employeePageDTO.getName() != null, Employee::getUsername, employeePageDTO.getName());
        queryWrapper.orderByDesc(Employee::getCreateTime);
        IPage<Employee> page = new Page<>(employeePageDTO.getPage(), employeePageDTO.getPageSize());
        IPage<Employee> employeeIPage = employeeService.page(page, queryWrapper);
        return Result.success(employeeIPage);
    }
    @Transactional(rollbackFor = Exception.class)
    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping
    public Result update(@RequestBody EmployeeDTO employeeDTO) {
        Employee employee = BeanUtil.toBean(employeeDTO, Employee.class);
        employeeService.updateById(employee);
        // 故意异常，编译不报错，runtime异常
//       Object obj = "hello";
//       Integer num = (Integer) obj;
//        String str = null;
//        str.length();
        return Result.success(OperationEnum.UPDATE +"--"+employeeDTO.getId());
    }
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids) {
        employeeService.removeByIds(ids);
        return Result.success(OperationEnum.DELETE+"--"+ids);
    }


}