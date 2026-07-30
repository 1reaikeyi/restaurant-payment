package pojo.dto.shopping;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.io.Serializable;

/**
 * 购物车DTO
 */
@Data
public class ShoppingCartDTO implements Serializable {

    /**
     * 菜品id
     */
    @TableField("dish_id")
    private Long dishId;
    
    /**
     * 套餐id
     */
    private Long setmealId;
    
    /**
     * 口味
     */
    private String dishFlavor;

    /**
     * 数量
     */
    private Long number;

}