package pojo.dto.setmeal;

import lombok.Data;
import pojo.entity.SetmealDish;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class SetmealDTO implements Serializable {

    private Long id;
    private Long categoryId;
    private String name;
    private BigDecimal price;
    private Long status;
    private String description;
    private String image;
    private List<SetmealDish> setmealDishes = new ArrayList<>();

}