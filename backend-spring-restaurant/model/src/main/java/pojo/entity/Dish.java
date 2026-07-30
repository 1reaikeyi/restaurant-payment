package pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("dish")
public class Dish implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 菜品名称
     */
    @TableField("name")
    private String name;

    /**
     * 菜品分类id
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 菜品价格
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 图片
     */
    @TableField("image")
    private String image;

    /**
     * 描述信息
     */
    @TableField("description")
    private String description;

    /**
     * 状态：0停售 1起售
     */
    @TableField("status")
    private Long status;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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