package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mapper.PlanMapper;
import org.springframework.stereotype.Service;
import model.entity.Plan;
import service.PlanService;

/**
 * 套餐 Service（对应 plan 表）
 */
@Service
public class PlanServiceImpl extends ServiceImpl<PlanMapper, Plan> implements PlanService {
}
