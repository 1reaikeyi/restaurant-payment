package pojo.dto.dish;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pojo.entity.DishFlavor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜品DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishDTO implements Serializable {

    /**
     * 主键
     */
    private Long id;
    
    /**
     * 菜品名称
     */
    private String name;
    
    /**
     * 菜品分类id
     */
    private Long categoryId;
    
    /**
     * 菜品价格
     */
    private BigDecimal price;
    
    /**
     * 图片
     */
    private String image;
    
    /**
     * 描述信息
     */
    private String description;
    
    /**
     * 状态：0停售 1起售
     */
    private Long status;
    
    /**
     * 口味
     */
    private List<DishFlavor> flavors = new ArrayList<>();

}