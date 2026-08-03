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
        Dish dish = BeanUtil.toBean(planDTO, Dish.class);
        planService.save(dish);
        List<DishDetail> dishDetailList = planDTO.getDishDetails().stream()
                .map(dishDetail -> BeanUtil.toBean(dishDetail, DishDetail.class))
                .toList();
        planDetailService.saveBatch(dishDetailList);
        return Result.success(OperationEnum.CREATE+"--"+dish.getId());
    }

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/all")
    public Result readAll(DishPageDTO dishPageDTO) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getStatus, StatusConstant.ENABLE)
                .like(dishPageDTO.getName() != null, Dish::getName, dishPageDTO.getName());
        IPage<Dish> page = new Page<>(dishPageDTO.getPage(), dishPageDTO.getPageSize());
        IPage<Dish> dishIPage = planService.page(page, queryWrapper);
        return Result.success(dishIPage);
    }

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping
    public Result readById(@RequestParam Long id) {
        Dish dish = planService.readCache(id);
        List<DishDetail> dishDetailList = planDetailService.lambdaQuery().eq(DishDetail::getDishId, dish.getId()).list();
        return Result.success(dish+"::"+dishDetailList);
    }

    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping
    public Result update(@RequestBody PlanDTO planDTO) {
        Dish dish = BeanUtil.toBean(planDTO, Dish.class);
        planService.updateCache(dish);
        return Result.success(OperationEnum.UPDATE+"--"+dish.getId());
    }

    @OperationLogging(operation = OperationEnum.DELETE)
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids) {
        planService.deleteCache(ids);
        return Result.success(OperationEnum.DELETE+"--"+ids);
    }
}
