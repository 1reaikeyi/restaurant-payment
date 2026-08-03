package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mapper.PlanMapper;
import model.dto.PlanDTO;
import org.springframework.stereotype.Service;
import model.entity.Plan;
import service.PlanService;

import java.util.List;

/**
 * 套餐 Service（对应 plan 表）
 */
@Service
public class PlanServiceImpl extends ServiceImpl<PlanMapper, Plan> implements PlanService {
    @Override
    public void updateCache(PlanDTO planDTO) {

    }

    @Override
    public void deleteCache(List<Long> ids) {

    }

    @Override
    public Plan readCache(Long id) {
        return null;
    }
}
