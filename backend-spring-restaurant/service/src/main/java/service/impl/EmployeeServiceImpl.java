package service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.constant.ErrorConstant;
import common.constant.StatusConstant;
import common.exception.BaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import pojo.dto.employee.EmployeeLoginDTO;
import pojo.entity.Employee;
import repository.EmployeeMapper;
import service.ISevcive.EmployeeService;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public Employee loadByEmployeeName(String userName) {
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUserName, userName);
        Employee employee = employeeMapper.selectOne(queryWrapper);
        return employee;
    }

    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String userName = employeeLoginDTO.getUserName();
        String password = employeeLoginDTO.getPassword();
        // TODO 后期需要进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //1、根据用户名查询数据库中的数据
        Employee employee = loadByEmployeeName(userName);

        //2、处理各种异常情况
        // 检查用户是否存在
        if (employee == null){
            throw new BaseException(ErrorConstant.ACCOUNT_NOT_EXIST);
        }
        //密码比对
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new BaseException(ErrorConstant.PASSWORD_ERROR);
        }
        //账号状态检查
        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new BaseException(ErrorConstant.ACCOUNT_LOCKED);
        }
        //3、返回实体对象
        return employee;
    }

}
