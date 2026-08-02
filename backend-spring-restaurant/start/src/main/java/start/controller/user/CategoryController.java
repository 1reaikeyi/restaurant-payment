package start.controller.user;

import common.result.Result;
import model.entity.RestaurantCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import service.RestaurantCategoryService;

import java.util.List;

@RestController
@RequestMapping("user/category")
@CacheConfig(cacheNames = "restaurantCategory:type")
public class CategoryController {
    @Autowired
    private RestaurantCategoryService restaurantCategoryService;
    @GetMapping
    @Cacheable(key = "#id",unless = "#result == null")
    public Result readById(@RequestParam Long id) {
        return Result.success(restaurantCategoryService.getById(id));
    }
    @Cacheable(key = "#type",unless = "#result == null")
    @GetMapping("/all")
    public Result readByType(@RequestParam("type") Long type) {
        List<RestaurantCategory> restaurantCategoryList = restaurantCategoryService.lambdaQuery().eq(RestaurantCategory::getType, type).list();
        return Result.success(restaurantCategoryList);
    }
}
