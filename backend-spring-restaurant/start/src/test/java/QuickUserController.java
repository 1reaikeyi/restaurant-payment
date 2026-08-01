import org.springframework.web.bind.annotation.*;
import model.entity.Employee;
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
