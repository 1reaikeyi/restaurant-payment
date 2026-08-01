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

/**
 * 菜品口味关系实体类（对应 dish_detail 表）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("dish_detail")
public class DishDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 菜品id
     */
    @TableField("dish_id")
    private Long dishId;

    /**
     * 标签
     */
    @TableField("key_type")
    private String keyType;

    /**
     * 标签详细，用,隔开
     */
    @TableField("value_list")
    private String valueList;

}
