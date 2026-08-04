package service;

import com.baomidou.mybatisplus.extension.service.IService;
import model.dto.DishDTO;
import model.entity.Dish;

import java.util.List;

/**
 * 菜品 Service（对应 dish 表）
 */

public interface DishService extends IService<Dish> {

    Dish readCache(Long id);

    void updateCache(DishDTO dishDTO);

    void deleteCache(List<Long> ids);

    void deleteCacheById(Long id);
}
