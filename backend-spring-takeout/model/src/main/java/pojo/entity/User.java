package pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 姓名
     */
    @TableField("username")
    private String userName;

    /**
     * 微信用户唯一标识
     */
    @JsonIgnore
    @TableField("openid")
    private String openId;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 性别：0女 1男
     */
    @TableField("sex")
    private String sex;

    /**
     * 身份证号
     */
    @TableField("id_number")
    private String idNumber;

    /**
     * 头像
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 注册时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /**
     * 是否删除：0否 1是
     */
    @TableField("is_delete")
    private Long isDelete;
}