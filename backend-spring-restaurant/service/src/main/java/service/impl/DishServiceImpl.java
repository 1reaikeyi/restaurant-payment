package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mapper.DishMapper;
import org.springframework.stereotype.Service;
import model.entity.Dish;
import service.DishService;

import java.util.List;

/**
 * 菜品 Service（对应 dish 表）
 */
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {


    @Override
    public Dish readCache(Long id) {
        return null;
    }

    @Override
    public void updateCache(Dish dish) {

    }

    @Override
    public void daleteCache(List<Long> ids) {

    }
}
