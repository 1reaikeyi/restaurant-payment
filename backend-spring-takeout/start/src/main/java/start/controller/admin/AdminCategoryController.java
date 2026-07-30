package start.controller.admin;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.constant.StatusConstant;
import common.result.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pojo.dto.category.CategoryDTO;
import pojo.dto.category.CategoryPageDTO;
import pojo.entity.Category;
import pojo.vo.CategoryVO;
import service.ISevcive.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/admin/categories")
@Transactional(rollbackFor = Exception.class)
public class AdminCategoryController {
    @Autowired
    private CategoryService categoryService;
    @PostMapping
    public Result createCategory(@RequestBody CategoryDTO categoryDTO){
        //根据名字查询分类
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getName, categoryDTO.getName());
        Category check_category = categoryService.getOne(wrapper);
        if(check_category!=null){
            return Result.error("分类名称已存在");
        }
        Category Category = BeanUtil.toBean(categoryDTO, Category.class);
        Category.setStatus(StatusConstant.ENABLE);
        // 设置默认的type值为1（如果前端没有传递type）
        if(Category.getType() == null){
            Category.setType(1L);
        }
        categoryService.save(Category);
        return Result.success("createCategory::"+Category.getId());
    }
    @GetMapping
    public Result page(CategoryPageDTO categoryPageDTO){
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 根据分类名称模糊查询
        if(categoryPageDTO.getName() != null && !categoryPageDTO.getName().isEmpty()){
            queryWrapper.like(Category::getName, categoryPageDTO.getName());
        }
        // 根据分类类型查询（如果传递了type参数）
        if(categoryPageDTO.getType() != null){
            queryWrapper.eq(Category::getType, categoryPageDTO.getType());
        }
        // 按排序字段升序排列
        queryWrapper.orderByAsc(Category::getSort);
        
        IPage<Category> iPage = new Page<>(categoryPageDTO.getPage(), categoryPageDTO.getPageSize());
        IPage<Category> categoryPage =  categoryService.page(iPage, queryWrapper);
        
        // 转换为VO对象，包含updateTime字段
        List<CategoryVO> voList = BeanUtil.copyToList(categoryPage.getRecords(), CategoryVO.class);
        IPage<CategoryVO> categoryVOPage = new Page<>(categoryPageDTO.getPage(), categoryPageDTO.getPageSize());
        categoryVOPage.setRecords(voList);
        categoryVOPage.setTotal(categoryPage.getTotal());
        categoryVOPage.setSize(categoryPage.getSize());
        categoryVOPage.setCurrent(categoryPage.getCurrent());
        
        return Result.success(categoryVOPage);
    }
    @GetMapping("/id/{id}")
    @Cacheable(cacheNames = "category", key = "#id")
    public Result byId(@PathVariable Long id){
        return Result.success(categoryService.getById(id));
    }

    @GetMapping("/type/{type}")
    @Cacheable(cacheNames = "category", key = "#type")
    public Result type(@PathVariable Long type){
        //根据类型查询分类
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getType, type);
        queryWrapper.orderByAsc(Category::getSort);
        List<Category> list = categoryService.list(queryWrapper);
        return Result.success(list);
    }
    @PutMapping("/{id}/status/{status}")
    @CacheEvict(cacheNames = "category", allEntries = true)
    public Result updateStatus(@PathVariable("id") Long id, @PathVariable Long status){
        Category category = Category.builder()
                .id(id).status(status)
                .build();
        categoryService.updateById(category);
        return Result.success(+id+"::"+(status == StatusConstant.ENABLE ? "启用成功" : "禁用成功"));
    }
    @PutMapping
    @CacheEvict(cacheNames = "category", allEntries = true)
    public Result updateCategory(@RequestBody CategoryDTO categoryDTO){
        LambdaQueryWrapper<Category> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Category::getId, categoryDTO.getId());
        if (!categoryService.exists(checkWrapper)) {
            return Result.error("更新失败，分类不存在");
        }
        Category category = BeanUtil.toBean(categoryDTO, Category.class);
        categoryService.updateById(category);
        return Result.success("updateCategory::" + category.getId());
    }
    @DeleteMapping("/{id}")
    @CacheEvict(cacheNames = "category", allEntries = true)
    public Result<String> deleteCategory(@PathVariable Long id){
        categoryService.removeById(id);
        return Result.success("deleteCategory::"+id);
    }
}