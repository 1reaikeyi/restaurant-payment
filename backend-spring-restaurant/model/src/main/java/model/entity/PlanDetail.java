package model.entity;

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
 * 套餐菜品关系实体类（对应 plan_detail 表）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("plan_detail")
public class PlanDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 对应套餐id
     */
    @TableField("plan_id")
    private Long planId;

    /**
     * 关联菜品id（多个用逗号分开）
     */
    @TableField("dish_id")
    private String dishId;

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
