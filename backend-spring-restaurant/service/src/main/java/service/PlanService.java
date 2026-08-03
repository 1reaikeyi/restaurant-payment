package service;

import com.baomidou.mybatisplus.extension.service.IService;
import model.entity.Dish;
import model.entity.Plan;

import java.util.List;

/**
 * 套餐 Service（对应 plan 表）
 */
public interface PlanService extends IService<Plan> {
    void updateCache(Dish dish);

    void deleteCache(List<Long> ids);

    Dish readCache(Long id);
}
