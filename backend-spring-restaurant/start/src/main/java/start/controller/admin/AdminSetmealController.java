package start.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import start.annotation.Info;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.constant.ErrorConstant;
import common.constant.StatusConstant;
import common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pojo.dto.setmeal.SetmealDTO;
import pojo.dto.setmeal.SetmealPageDTO;
import pojo.entity.Setmeal;
import pojo.entity.SetmealDish;
import service.ISevcive.SetmealDishService;
import service.ISevcive.SetmealService;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/setmeals")
@Transactional(rollbackFor = Exception.class)
public class AdminSetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;

    @Info(desc = "添加套餐")
    @PostMapping
    @CacheEvict(cacheNames = "setmeal", allEntries = true)
    public Result addSetmeal(@RequestBody SetmealDTO setmealDTO) {
        LambdaQueryWrapper<Setmeal> setmealWrapper = new LambdaQueryWrapper<>();
        setmealWrapper.eq(Setmeal::getName, setmealDTO.getName());
        Setmeal check_setmeal = setmealService.getOne(setmealWrapper);
        if (check_setmeal != null) {
            return Result.error("套餐名称已存在");
        }
        List<SetmealDish> check_setmealDishes = setmealDTO.getSetmealDishes();
        if (check_setmealDishes == null || check_setmealDishes.isEmpty()) {
            return Result.error("套餐菜品不能为空");
        }
        Setmeal setmeal = BeanUtil.toBean(setmealDTO, Setmeal.class);
        setmeal.setStatus(StatusConstant.ENABLE);
        setmealService.save(setmeal);
        List<SetmealDish> setmealDishesList = setmealDTO.getSetmealDishes().stream()
                .map(new Function<SetmealDish, SetmealDish>() {
                    @Override
                    public SetmealDish apply(SetmealDish setmealDish) {
                        SetmealDish s = SetmealDish.builder()
                                .setmealId(setmeal.getId())
                                .copies(setmealDish.getCopies())
                                .dishId(setmealDish.getDishId())
                                .name(setmealDish.getName())
                                .build();
                        return s;
                    }

                })
                .collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishesList);
        return Result.success("addSetmeal::"+setmeal.getId());
    }
    @Info(desc = "查询detail")
    @GetMapping("/{id}")
    @Cacheable(cacheNames = "setmeal", key = "#id")
    public Result readSetmeal(@PathVariable Long id) {
        LambdaQueryWrapper<Setmeal> setmealWrapper = new LambdaQueryWrapper<>();
        setmealWrapper.eq(Setmeal::getId, id);
        Setmeal setmeal = setmealService.getOne(setmealWrapper);
        if (setmeal == null) {
            return Result.error("套餐"+ ErrorConstant.NULL_ERROR);
        }
        LambdaQueryWrapper<SetmealDish> setmealDishWrapper = new LambdaQueryWrapper<>();
        setmealDishWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishList = setmealDishService.list(setmealDishWrapper);
        return Result.success(setmealDishList);
    }
    @GetMapping
    public Result page(SetmealPageDTO setmealPageDTO) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        // 根据套餐名称模糊查询
        if(setmealPageDTO.getName() != null && !setmealPageDTO.getName().isEmpty()){
            queryWrapper.like(Setmeal::getName, setmealPageDTO.getName());
        }
        // 根据分类ID查询
        if(setmealPageDTO.getCategoryId() != null){
            queryWrapper.eq(Setmeal::getCategoryId, setmealPageDTO.getCategoryId());
        }
        // 根据状态查询（如果传递了status参数）
        if(setmealPageDTO.getStatus() != null){
            queryWrapper.eq(Setmeal::getStatus, setmealPageDTO.getStatus());
        }
        // 按分类ID排序，同一分类内按排序字段排序
        queryWrapper.orderByAsc(Setmeal::getCategoryId);
        
        IPage<Setmeal> setmealIPage = new Page<>(setmealPageDTO.getPage(), setmealPageDTO.getPageSize());
        IPage<Setmeal> pageResult =  setmealService.page(setmealIPage, queryWrapper);
        return Result.success(pageResult);
    }
    @PutMapping("/{id}/status/{status}")
    @CacheEvict(cacheNames = "setmeal", key = "#id")
    @Info(desc = "更改套餐状态")
    public Result updateStatus(@PathVariable Long id, @PathVariable Long status) {
        Setmeal setmeal = Setmeal.builder().id(id).status(status).build();
        setmealService.updateById(setmeal);
        return Result.success(id+"::"+(status == StatusConstant.ENABLE?"启用成功":"停用成功"));
    }
    @PutMapping
    @CacheEvict(cacheNames = "setmeal", key = "#setmealDTO.id")
    public Result updateSetmeal(@RequestBody SetmealDTO setmealDTO) {
        Setmeal setmeal = BeanUtil.toBean(setmealDTO, Setmeal.class);
        setmealService.updateById(setmeal);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        List<SetmealDish> setmealDishList = setmealDishes.stream()
                .map(dto -> SetmealDish.builder()
                        .setmealId(setmealDTO.getId())
                        .dishId(dto.getDishId().longValue())
                        .name(dto.getName())
                        .price(dto.getPrice())
                        .copies(dto.getCopies().longValue())
                        .build())
                .collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishList);
        return Result.success("updateSetmeal::"+setmealDTO.getId());
    }
    @DeleteMapping
    @CacheEvict(cacheNames = "setmeal", allEntries = true)
    public Result deleteSetmealAll(@RequestParam List<Long> ids) {
        setmealService.removeByIds(ids);
        return Result.success("deleteSetmealAll::"+ids);
    }
}