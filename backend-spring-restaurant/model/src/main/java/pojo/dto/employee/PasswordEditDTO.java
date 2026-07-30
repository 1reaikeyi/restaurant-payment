package pojo.dto.employee;

import lombok.Data;

import java.io.Serializable;

/**
 * 密码修改DTO
 */
@Data
public class PasswordEditDTO implements Serializable {

    /**
     * 员工id
     */
    private Long empId;

    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;
    /**
     * 确认密码
     */
    private String confirmPassword;

}