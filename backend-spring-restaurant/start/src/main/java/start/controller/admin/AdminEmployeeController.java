package start.controller.admin;




import common.properties.JwtProperties;
import common.result.Result;
import model.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.EmployeeService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/employee")
public class AdminEmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("/register")
    public Result register(@RequestBody Employee employee) {
        return Result.success();
    }
    @PostMapping("/login")
    public Result login() {
        return Result.success();
    }
    @PostMapping("/logout")
    public Result logout() {
        return Result.success();
    }


}