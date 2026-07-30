package start;

import org.springframework.web.bind.annotation.*;
import pojo.entity.Employee;
import start.annotation.Info;

@CrossOrigin
@RestController
public class QuickUserController {
    @PostMapping("/register")
    public Employee register(@RequestBody Employee user) {
        return user;
    }
    @Info
    @PostMapping("/login")
    public Employee login(@RequestBody Employee employee) {
        System.out.println(employee);
        return employee;
    }
}
