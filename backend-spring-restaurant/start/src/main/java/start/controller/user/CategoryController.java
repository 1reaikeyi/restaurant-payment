package start.controller.user;

import common.enumOperation.OperationEnum;
import common.result.Result;
import model.entity.Dish;
import model.entity.Plan;
import model.entity.RestaurantCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import service.DishDetailService;
import service.DishService;
import service.PlanService;
import service.RestaurantCategoryService;
import start.aop.OperationLogging;

import java.util.List;

@RestController
@RequestMapping("user/category")
@CacheConfig(cacheNames = "restaurantCategory:type")
public class CategoryController {
    @Autowired
    private RestaurantCategoryService restaurantCategoryService;
    @Autowired
    private DishService dishService;
    @Autowired
    private PlanService planService;

    @OperationLogging(operation = OperationEnum.READ)
    @Cacheable(key = "#id")
    @GetMapping
    public Result readById(@RequestParam Long id) {
        return Result.success(restaurantCategoryService.getById(id));
    }
    @OperationLogging(operation = OperationEnum.READ)
    @Cacheable(key = "#type")
    @GetMapping("/all")
    public Result readByType(@RequestParam("type") Long type) {
        List<RestaurantCategory> restaurantCategoryList = restaurantCategoryService.lambdaQuery().eq(RestaurantCategory::getType, type).list();
        return Result.success(restaurantCategoryList);
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
