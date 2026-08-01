package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mapper.DishMapper;
import org.springframework.stereotype.Service;
import model.entity.Dish;
import service.DishService;

/**
 * 菜品 Service（对应 dish 表）
 */
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {


}
