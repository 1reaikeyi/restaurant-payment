package start.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.constant.StatusConstant;
import common.enumOperation.OperationEnum;
import common.result.Result;
import model.dto.CategoryPageDTO;
import model.dto.EmployeePageDTO;
import model.dto.RestaurantCategoryDTO;
import model.entity.Dish;
import model.entity.Plan;
import model.entity.RestaurantCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import service.DishService;
import service.PlanService;
import service.RestaurantCategoryService;
import start.aop.OperationLogging;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
@CacheConfig(cacheNames = "restaurantCategory:type")
public class AdminCategoryController {

    @Autowired
    private RestaurantCategoryService restaurantCategoryService;
    @Autowired
    private DishService dishService;
    @Autowired
    private PlanService planService;

    @OperationLogging(operation = OperationEnum.CREATE)
    @PostMapping
    public Result create(@RequestBody RestaurantCategoryDTO restaurantCategoryDTO) {
        RestaurantCategory restaurantCategory = BeanUtil.toBean(restaurantCategoryDTO, RestaurantCategory.class);
        restaurantCategoryService.save(restaurantCategory);
        return Result.success(OperationEnum.CREATE+"--"+restaurantCategory.getId());
    }

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/all")
    public Result readAll(CategoryPageDTO categoryPageDTO) {
        LambdaQueryWrapper<RestaurantCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RestaurantCategory::getType, categoryPageDTO.getType())
                .eq(RestaurantCategory::getStatus, StatusConstant.ENABLE)
                .like(categoryPageDTO.getName() != null, RestaurantCategory::getName, categoryPageDTO.getName());
        IPage<RestaurantCategory> page = new Page<>(categoryPageDTO.getPage(), categoryPageDTO.getPageSize());
        IPage<RestaurantCategory> restaurantCategoryIPage = restaurantCategoryService.page(page, queryWrapper);
        return Result.success(restaurantCategoryIPage);
    }

    @OperationLogging(operation = OperationEnum.READ)
    @Cacheable(key = "#type")
    @GetMapping
    public Result readByType(@RequestParam("type") Long type) {
        List<RestaurantCategory> restaurantCategoryList = restaurantCategoryService.lambdaQuery().eq(RestaurantCategory::getType, type).list();
        return Result.success(restaurantCategoryList);
    }

    @OperationLogging(operation = OperationEnum.UPDATE)
    @CacheEvict(allEntries = true)
    @PutMapping
    public Result update(@RequestBody RestaurantCategoryDTO restaurantCategoryDTO) {
        RestaurantCategory restaurantCategory = BeanUtil.toBean(restaurantCategoryDTO, RestaurantCategory.class);
        restaurantCategoryService.updateById(restaurantCategory);
        return Result.success(OperationEnum.UPDATE+"--"+restaurantCategory.getId());
    }

    @OperationLogging(operation = OperationEnum.DELETE)
    @CacheEvict(allEntries = true)
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids) {
        restaurantCategoryService.removeByIds(ids);
        return Result.success(OperationEnum.DELETE+"--"+ids);
    }

    @OperationLogging(operation = OperationEnum.READ)
    @Cacheable(key = "#categoryId")
    @GetMapping("/of/dish")
    public Result readDish(@RequestParam("id") Long categoryId) {
        List<Dish> dishList = dishService.lambdaQuery().eq(Dish::getCategoryId,categoryId).list();
        return Result.success(dishList);
    }
    @OperationLogging(operation = OperationEnum.READ)
    @Cacheable(key = "#categoryId")
    @GetMapping("of/plan")
    public Result getPlan(@RequestParam("id") Long categoryId) {
        List<Plan> planList = planService.lambdaQuery().in(Plan::getCategoryId, categoryId).list();
        return Result.success(planList);
    }
}
