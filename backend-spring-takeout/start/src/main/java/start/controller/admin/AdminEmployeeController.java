package start.controller.admin;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.constant.JwtClaimsConstant;
import common.constant.ErrorConstant;
import common.constant.PasswordConstant;
import common.constant.StatusConstant;
import common.localContextHolder.ThreadLocalContextHolder;
import common.properties.JwtProperties;
import common.result.Result;
import common.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import pojo.dto.employee.EmoployeePageDTO;
import pojo.dto.employee.EmployeeDTO;
import pojo.dto.employee.EmployeeLoginDTO;
import pojo.dto.employee.PasswordEditDTO;
import pojo.entity.Employee;
import service.ISevcive.EmployeeService;
import start.annotation.Info;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/admin/employees")
@Transactional(rollbackFor = Exception.class)
public class AdminEmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private JwtProperties jwtProperties;
    private static final String KEY_PREFIX = "emp:";

    @PostMapping("/register")
    public Result register(@RequestBody EmployeeDTO employeeDTO) {
        Employee check_employee = employeeService.loadByEmployeeName(employeeDTO.getUserName());
        if (check_employee != null) {
            return Result.error(ErrorConstant.USERNAME_EXIST);
        }
        // 密码加密
        employeeDTO.setPassword(DigestUtils.md5DigestAsHex(employeeDTO.getPassword().getBytes()));
        Employee employee = BeanUtil.toBean(employeeDTO, Employee.class);
        employeeService.save(employee);
        return Result.success("register::"+employee.getId());
    }

    @Info(desc = "登录")
    @PostMapping("/login")
    public Result login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        Employee employee = employeeService.login(employeeLoginDTO);
        if (employee == null) {
            return Result.error(ErrorConstant.PASSWORD_ERROR);
        }
        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        Long empId = employee.getId();
        String empName = employee.getUserName();
        claims.put(JwtClaimsConstant.EMP_ID, empId);
        claims.put(JwtClaimsConstant.EMPNAME, empName);
        ThreadLocalContextHolder.set(claims);
        String token = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), claims);
        stringRedisTemplate.opsForValue().set(KEY_PREFIX +employee.getId(),token,jwtProperties.getAdminTtl(), TimeUnit.SECONDS);
        return Result.success(token);
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

    @PostMapping
    public Result createEmployee(@RequestBody EmployeeDTO employeeDTO) {
        Employee check_employee = employeeService.loadByEmployeeName(employeeDTO.getUserName());
        if (check_employee != null) {
            return Result.error(ErrorConstant.USERNAME_EXIST);
        }
        if (employeeDTO.getPassword() == null) {
            String password = PasswordConstant.PASSWORD;
            employeeDTO.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
        }
        Employee employee = BeanUtil.toBean(employeeDTO, Employee.class);
        employee.setStatus(StatusConstant.ENABLE);
        employeeService.save(employee);
        return Result.success("createEmployee::"+employee.getId());
    }
    @GetMapping("/{id}")
    public Result readEmployee(@PathVariable Long id) {
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }
    @GetMapping
    public Result page(EmoployeePageDTO emoployeePageDTO) {
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        // 根据用户名模糊查询
        if(emoployeePageDTO.getUserName() != null && !emoployeePageDTO.getUserName().isEmpty()){
            queryWrapper.like(Employee::getUserName, emoployeePageDTO.getUserName());
        }
        // 按ID降序排列（最新的在前）
        queryWrapper.orderByDesc(Employee::getId);
        
        IPage<Employee> employeeIPage = new Page<>(emoployeePageDTO.getPage(),
                emoployeePageDTO.getPageSize());
        IPage<Employee> page = employeeService.page(employeeIPage, queryWrapper);
        return Result.success(page);
    }

    @PutMapping("/{id}/status/{status}")
    public Result updateStatus(@PathVariable Long id, @PathVariable Long status) {
        Employee employee = Employee.builder()
                .id(id).status(status)
                .build();
        employeeService.updateById(employee);
        return Result.success("id::"+(status == StatusConstant.ENABLE ? "启用" : "禁用"));
    }

    @PutMapping
    public Result updateEmployee(@RequestBody EmployeeDTO employeeDTO) {
        // 检查用户名是否被其他员工使用（排除当前编辑的员工）
        LambdaQueryWrapper<Employee> Wrapper = new LambdaQueryWrapper<>();
        Wrapper.eq(Employee::getUserName, employeeDTO.getUserName())
               .ne(employeeDTO.getId() != null, Employee::getId, employeeDTO.getId());
        Employee check_employee = employeeService.getOne(Wrapper);
        if (check_employee != null) {
            return Result.error(ErrorConstant.USERNAME_EXIST);
        }
        Employee employee = BeanUtil.toBean(employeeDTO, Employee.class);
        employeeService.updateById(employee);
        return Result.success("updateEmployee::"+ employee.getId());
    }

    @PutMapping("/password")
    public Result updatePassword(@RequestBody PasswordEditDTO passwordDTO) {
        Employee employee = employeeService.getById(passwordDTO.getEmpId());
        employee.setPassword(DigestUtils.md5DigestAsHex(passwordDTO.getConfirmPassword().getBytes()));
        employeeService.updateById(employee);
        return Result.success("updateStatus::"+passwordDTO.getEmpId());
    }

    @DeleteMapping("/{id}")
    public Result deleteEmployee(@PathVariable Long id) {
        employeeService.removeById(id);
        return Result.success("deleteEmployee::"+id);
    }
    @DeleteMapping
    public Result deleteAll(@RequestParam List<Long> ids) {
        employeeService.removeByIds(ids);
        return Result.success("deleteAll::"+ids);
    }
}