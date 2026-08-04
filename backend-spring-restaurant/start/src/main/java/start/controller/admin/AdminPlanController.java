package start.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.constant.StatusConstant;
import common.enumOperation.OperationEnum;
import common.result.Result;
import model.dto.PlanDTO;
import model.dto.DishPageDTO;
import model.entity.Dish;
import model.entity.DishDetail;
import model.entity.Plan;
import model.entity.PlanDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import service.PlanDetailService;
import service.PlanService;
import start.aop.OperationLogging;

import java.util.List;

@RestController
@RequestMapping("/admin/plan")
public class AdminPlanController {
    @Autowired
    private PlanService planService;
    @Autowired
    private PlanDetailService planDetailService;
    @OperationLogging(operation = OperationEnum.CREATE)
    @Transactional(rollbackFor = Exception.class)
    @PostMapping
    public Result create(@RequestBody PlanDTO planDTO) {
        Plan plan = BeanUtil.toBean(planDTO, Plan.class);
        planService.save(plan);
        // 清除可能残留的空值缓存（防穿透），避免复用同一 id 时读到旧空值
        planService.deleteCacheById(plan.getId());
        List<PlanDetail> planDetailList = planDTO.getPlanDetails().stream()
                .map(planDetail -> BeanUtil.toBean(planDetail, PlanDetail.class))
                .toList();
        planDetailService.saveBatch(planDetailList);
        return Result.success(OperationEnum.CREATE+"--"+plan.getId());
    }

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/all")
    public Result readAll(DishPageDTO dishPageDTO) {
        LambdaQueryWrapper<Plan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Plan::getStatus, StatusConstant.ENABLE)
                .like(dishPageDTO.getName() != null, Plan::getName, dishPageDTO.getName());
        IPage<Plan> page = new Page<>(dishPageDTO.getPage(), dishPageDTO.getPageSize());
        IPage<Plan> planPage = planService.page(page, queryWrapper);
        return Result.success(planPage);
    }

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping
    public Result readById(@RequestParam Long id) {
        Plan plan = planService.readCache(id);
        if (plan == null) {
            return Result.error("套餐不存在");
        }
        List<PlanDetail> planDetailList = planDetailService.lambdaQuery().eq(PlanDetail::getPlanId, plan.getId()).list();
        return Result.success(plan+"::"+planDetailList);
    }

    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping
    public Result update(@RequestBody PlanDTO planDTO) {
        planService.updateCache(planDTO);
        return Result.success(OperationEnum.UPDATE+"--"+planDTO.getId());
    }

    @OperationLogging(operation = OperationEnum.DELETE)
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids) {
        planService.deleteCache(ids);
        return Result.success(OperationEnum.DELETE+"--"+ids);
    }
}
