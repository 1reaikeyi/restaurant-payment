package pojo.dto.employee;



import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 员工登录DTO
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeLoginDTO implements Serializable {
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 密码
     */
    private String password;
}