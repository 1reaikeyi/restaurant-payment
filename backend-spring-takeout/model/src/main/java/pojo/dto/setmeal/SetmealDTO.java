package pojo.dto.setmeal;

import lombok.Data;
import pojo.entity.SetmealDish;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class SetmealDTO implements Serializable {
    /**
     * 主键
     */
    private Long id;
    /**
     * 分类id
     */
    private Long categoryId;
    /**
     * 名称
     */
    private String name;
    /**
     * 价格
     */
    private BigDecimal price;
    /**
     * 状态
     */
    private Long status;
    /**
     * 描述
     */
    private String description;
    /**
     * 图片
     */
    private String image;
    /**
     * 配置的菜品
     */
    private List<SetmealDish> setmealDishes = new ArrayList<>();

}