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
import pojo.vo.CategoryVO;
import service.ISevcive.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user/categories")
public class UserCategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/id/{id}")
    public Result readCategoryById(@PathVariable Long id) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getId, id)
                .eq(Category::getStatus, StatusConstant.ENABLE)
                .orderByAsc(Category::getSort);
        List<Category> categoryList = categoryService.list(queryWrapper);
        if (categoryList == null || categoryList.size() == 0) {
            return Result.error(ErrorConstant.NULL_ERROR);
        }
        List<CategoryVO> categoryVOList = categoryList.stream()
                .map((Category category) ->{return CategoryVO.builder()
                        .id(category.getId()).type(category.getType()).name(category.getName()).sort(category.getSort())
                        .status(category.getStatus())
                        .build();}
                )
                .collect(Collectors.toList());
        return Result.success(categoryVOList);
    }
    @GetMapping("/type/{type}")
    public Result getCategoryByType(@PathVariable Long type) {
        LambdaQueryWrapper<Category> typeWrapper = new LambdaQueryWrapper<>();
        typeWrapper.eq(Category::getType, type)
                .eq(Category::getStatus, StatusConstant.ENABLE)
                .orderByAsc(Category::getSort);
        List<Category> categoryList = categoryService.list(typeWrapper);
        if (categoryList == null || categoryList.size() == 0) {
            return Result.error(ErrorConstant.NULL_ERROR);
        }
        List<CategoryVO> categoryVOList = categoryList.stream()
                .map((Category category) ->{return CategoryVO.builder()
                        .id(category.getId()).type(category.getType()).name(category.getName()).sort(category.getSort())
                        .status(category.getStatus())
                        .build();}
                )
                .collect(Collectors.toList());
        return Result.success(categoryVOList);
    }
}
