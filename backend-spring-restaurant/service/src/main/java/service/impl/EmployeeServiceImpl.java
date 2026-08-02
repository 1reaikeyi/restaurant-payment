package service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mapper.EmployeeMapper;
import org.springframework.stereotype.Service;
import model.entity.Employee;
import service.EmployeeService;

/**
 * 员工 Service（对应 employee 表）
 */
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Override
    public Employee findEmployeename(String username) {
        return super.lambdaQuery().eq(Employee::getUsername, username).one();
    }

}
