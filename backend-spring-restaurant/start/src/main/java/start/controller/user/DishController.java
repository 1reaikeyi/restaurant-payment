package start.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.constant.StatusConstant;
import common.enumOperation.OperationEnum;
import common.result.Result;
import model.dto.DishPageDTO;
import model.entity.Dish;
import model.entity.DishDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import service.DishDetailService;
import service.DishService;
import start.aop.OperationLogging;

import java.util.List;

@RestController
@RequestMapping("/user/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishDetailService dishDetailService;

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/all")
    public Result readAll(DishPageDTO dishPageDTO) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getStatus, StatusConstant.ENABLE)
                .like(dishPageDTO.getName() != null, Dish::getName, dishPageDTO.getName());
        IPage<Dish> page = new Page<>(dishPageDTO.getPage(), dishPageDTO.getPageSize());
        IPage<Dish> dishIPage = dishService.page(page, queryWrapper);
        return Result.success(dishIPage);
    }

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping
    public Result readById(@RequestParam Long id) {
        Dish dish = dishService.readCache(id);
        List<DishDetail> dishDetailList = dishDetailService.lambdaQuery().eq(DishDetail::getDishId, dish.getId()).list();
        return Result.success(dish+"::"+dishDetailList);
    }
}
