package start.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.constant.StatusConstant;
import common.enumOperation.OperationEnum;
import common.result.Result;
import model.dto.CategoryPageDTO;
import model.dto.DishDTO;
import model.dto.DishPageDTO;
import model.entity.Dish;
import model.entity.DishDetail;
import model.entity.RestaurantCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import service.DishDetailService;
import service.DishService;
import start.aop.OperationLogging;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
public class AdminDishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishDetailService dishDetailService;

    @OperationLogging(operation = OperationEnum.CREATE)
    @Transactional(rollbackFor = Exception.class)
    @PostMapping
    public Result create(@RequestBody DishDTO dishDTO) {
        Dish dish = BeanUtil.toBean(dishDTO, Dish.class);
        dishService.save(dish);
        List<DishDetail> dishDetailList = dishDTO.getDishDetails().stream()
                .map(dishDetail -> BeanUtil.toBean(dishDetail, DishDetail.class))
                .toList();
        dishDetailService.saveBatch(dishDetailList);
        return Result.success(OperationEnum.CREATE+"--"+dish.getId());
    }
    
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

    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping
    public Result update(@RequestBody DishDTO dishDTO) {
        dishService.updateCache(dishDTO);
        return Result.success(OperationEnum.UPDATE+"--"+dishDTO.getId());
    }

    @OperationLogging(operation = OperationEnum.DELETE)
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids) {
        dishService.deleteCache(ids);
        return Result.success(OperationEnum.DELETE+"--"+ids);
    }
}
