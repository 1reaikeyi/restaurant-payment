package pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单明细实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("order_detail")
public class OrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 名称
     */
    @TableField("name")
    private String name;

    /**
     * 订单id
     */
    @TableField("order_id")
    private Long orderId;

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
     * 打包费
     */
    @TableField("pack_amount")
    private BigDecimal packAmount;

    /**
     * 餐具数量
     */
    @TableField("tableware_number")
    private Long tablewareNumber;

}