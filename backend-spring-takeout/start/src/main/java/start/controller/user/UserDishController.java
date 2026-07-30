package start.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import common.constant.ErrorConstant;
import common.constant.StatusConstant;
import common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pojo.entity.Category;
import pojo.entity.Dish;
import pojo.entity.DishFlavor;
import pojo.vo.DishVO;
import service.ISevcive.CategoryService;
import service.ISevcive.DishFlavorService;
import service.ISevcive.DishService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user/dishes")
public class UserDishController {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @GetMapping("/category/{categoryId}")
    public Result readDishByCategory(@PathVariable Long categoryId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getId, categoryId)
                .select(Category::getName);
        Category category = categoryService.getOne(wrapper);
        if (category == null) {
            return Result.error(ErrorConstant.NULL_ERROR);
        }
        LambdaQueryWrapper<Dish> dishWrapper = new LambdaQueryWrapper<>();
        dishWrapper.eq(Dish::getCategoryId, categoryId)
                .eq(Dish::getStatus, StatusConstant.ENABLE);
        List<Dish> dishList = dishService.list(dishWrapper);
        if (dishList == null || dishList.size() == 0) {
            return Result.error(ErrorConstant.NULL_ERROR);
        }
        List<Long> dishIds = dishList.stream().map((Dish dish) -> dish.getId()).collect(Collectors.toList());
        LambdaQueryWrapper<DishFlavor> dishFlavorWrapper = new LambdaQueryWrapper<>();
        dishFlavorWrapper.in(DishFlavor::getDishId, dishIds)
                .select(DishFlavor::getName, DishFlavor::getValue, DishFlavor::getDishId);
        List<DishFlavor> dishFlavorList = dishFlavorService.list(dishFlavorWrapper);
        if (dishFlavorList == null || dishFlavorList.size() == 0) {
            return Result.error(ErrorConstant.NULL_ERROR);
        }
        // 按菜品ID分组口味（一个菜品对应多个口味）
        Map<Long, List<DishFlavor>> flavorMap = dishFlavorList.stream()
                .collect(Collectors.groupingBy(DishFlavor::getDishId));
        // 封装DishVO（核心修复：遍历菜品，而非遍历口味）
        List<DishVO> dishVOList = dishList.stream()
                .map((Dish dish) ->{return DishVO.builder()
                    .id(dish.getId()).name(dish.getName()).categoryId(dish.getCategoryId()).price(dish.getPrice()).image(dish.getImage()).description(dish.getDescription())
//                    .status(dish.getStatus()).updateTime(dish.getUpdateTime()).categoryName(category.getName())
                    .flavors(flavorMap.getOrDefault(dish.getId(), List.of()))
                    .build();}
                )
                .collect(Collectors.toList());
        return Result.success(dishVOList);
    }
}
