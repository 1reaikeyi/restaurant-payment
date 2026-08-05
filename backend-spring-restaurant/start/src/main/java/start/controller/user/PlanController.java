package start.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.constant.StatusConstant;
import common.enumOperation.OperationEnum;
import common.result.Result;
import model.dto.DishPageDTO;
import model.entity.Dish;
import model.entity.Plan;
import model.entity.PlanDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import service.DishService;
import service.PlanDetailService;
import service.PlanService;
import start.aop.OperationLogging;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/user/plan")
public class PlanController {
    @Autowired
    private PlanService planService;
    @Autowired
    private PlanDetailService planDetailService;
    @Autowired
    private DishService dishService;

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
    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("of/dish")
    public Result getDish(@RequestParam Long id) {
        List<PlanDetail> planDetailList = planDetailService.lambdaQuery().eq(PlanDetail::getPlanId, id).list();
        List<List<Dish>> dishes = new ArrayList<>();
        for (PlanDetail planDetail1 : planDetailList) {
            Integer[] dishId = planService.ofDishId(planDetail1);
            if (dishId == null && dishId.length < 0) {
                continue;
            }
            List<Dish> dishList = dishService.lambdaQuery().in(Dish::getId, dishId).list();
            dishes.add(dishList);
        }
        return Result.success(dishes);
    }
}
