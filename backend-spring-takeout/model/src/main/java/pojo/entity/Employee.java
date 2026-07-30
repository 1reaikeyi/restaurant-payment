package pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("employee")
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    @TableField("username")
    private String userName;

    /**
     * 姓名
     */
    @TableField("name")
    private String name;
    
    /**
     * 密码
     */
    @JsonIgnore
    @TableField("password")
    private String password;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 性别
     */
    @TableField("sex")
    private String sex;
    
    /**
     * 身份证号
     */
    @TableField("id_number")
    private String idNumber;

    /**
     * 状态
     */
    @TableField("status")
    private Long status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @TableField(value = "create_user",fill = FieldFill.INSERT)
    private Long createUser;
    
    /**
     * 修改人
     */
    @TableField(value = "update_user",fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

}