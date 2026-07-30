package repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;
import pojo.entity.Dish;

@Repository
public interface DishMapper extends BaseMapper<Dish> {

}
