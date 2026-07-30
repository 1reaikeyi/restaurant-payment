package pojo.dto.user;

import lombok.Data;

import java.io.Serializable;
@Data
public class UserLogInDTO implements Serializable {
    /**
     * 验证码
     */
    private String securityCode;
    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;
}
