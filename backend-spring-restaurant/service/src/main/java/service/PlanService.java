package service;

import com.baomidou.mybatisplus.extension.service.IService;
import model.dto.PlanDTO;
import model.entity.Plan;

import java.util.List;

/**
 * 套餐 Service（对应 plan 表）
 */
public interface PlanService extends IService<Plan> {
    void updateCache(PlanDTO planDTO);

    void deleteCache(List<Long> ids);

    void deleteCacheById(Long id);

    Plan readCache(Long id);
}
