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
 * 套餐实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("setmeal")
public class Setmeal implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分类id
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 套餐名称
     */
    @TableField("name")
    private String name;

    /**
     * 套餐价格
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 状态：0停用 1启用
     */
    @TableField("status")
    private Long status;

    /**
     * 描述信息
     */
    @TableField("description")
    private String description;

    /**
     * 图片
     */
    @TableField("image")
    private String image;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    /**
     * 创建人
     */
    @TableField(value = "create_user", fill = FieldFill.INSERT)
    private Long createUser;
    
    /**
     * 修改人
     */
    @TableField(value = "update_user", fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}