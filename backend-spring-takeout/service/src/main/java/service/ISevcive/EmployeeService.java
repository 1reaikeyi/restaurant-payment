package service.ISevcive;


import com.baomidou.mybatisplus.extension.service.IService;
import pojo.dto.employee.EmployeeLoginDTO;
import pojo.entity.Employee;

public interface EmployeeService extends IService<Employee> {
    Employee loadByEmployeeName(String userName);
    Employee login(EmployeeLoginDTO employeeLoginDTO);
}
