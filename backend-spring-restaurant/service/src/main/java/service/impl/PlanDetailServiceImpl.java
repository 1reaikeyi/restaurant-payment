package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mapper.PlanDetailMapper;
import org.springframework.stereotype.Service;
import model.entity.PlanDetail;
import service.PlanDetailService;

/**
 * 套餐菜品关系 Service（对应 plan_detail 表）
 */
@Service
public class PlanDetailServiceImpl extends ServiceImpl<PlanDetailMapper, PlanDetail> implements PlanDetailService {
}
