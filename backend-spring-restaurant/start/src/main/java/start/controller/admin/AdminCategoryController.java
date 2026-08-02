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
import model.entity.RestaurantCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import service.RestaurantCategoryService;
import start.aop.OperationLogging;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
@CacheConfig(cacheNames = "restaurantCategory:type")
public class AdminCategoryController {
    @Autowired
    private RestaurantCategoryService restaurantCategoryService;
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

    @Cacheable(key = "#type",unless = "#result == null")
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
}
