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
 * 套餐菜品关系实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("setmeal_dish")
public class SetmealDish implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 套餐id
     */
    @TableField("setmeal_id")
    private Long setmealId;

    /**
     * 菜品id
     */
    @TableField("dish_id")
    private Long dishId;

    /**
     * 菜品名称（冗余字段）
     */
    @TableField("name")
    private String name;

    /**
     * 菜品原价
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 份数
     */
    @TableField("copies")
    private Long copies;
}