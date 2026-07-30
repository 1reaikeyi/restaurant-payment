package pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("shopping_cart")
public class ShoppingCart implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 名称
     */
    @TableField("name")
    private String name;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 菜品id
     */
    @TableField("dish_id")
    private Long dishId;

    /**
     * 套餐id
     */
    @TableField("setmeal_id")
    private Long setmealId;

    /**
     * 口味
     */
    @TableField("dish_flavor")
    private String dishFlavor;

    /**
     * 数量
     */
    @TableField("number")
    private Long number;

    /**
     * 金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 图片
     */
    @TableField("image")
    private String image;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime createTime;
}