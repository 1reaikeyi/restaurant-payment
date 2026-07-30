package pojo.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * C端用户登录DTO
 */
@Data
public class UserDTO implements Serializable {
//    id,username,openid,phone,sex,id_number,avatar,create_time
    /**
     * 验证码
     */
    private String code;

    /**
     * 姓名
     */
    private String userName;
    /**
     * 密码
     */
    private String openId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 性别
     */
    private String sex;
    /**
     * 身份证号
     */
    private String idNumber;
    /**
     * 头像
     */
    private String avatar;
}