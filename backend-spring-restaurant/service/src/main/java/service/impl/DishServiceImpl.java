package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Repository;
import pojo.entity.Dish;
import repository.DishMapper;
import service.ISevcive.DishService;

@Repository
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

}