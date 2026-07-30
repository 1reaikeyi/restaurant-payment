package start.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pojo.entity.Setmeal;
import pojo.entity.SetmealDish;
import service.ISevcive.SetmealDishService;
import service.ISevcive.SetmealService;

import java.util.List;

@RestController
@RequestMapping("/user/setmeals")
public class UserSetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @GetMapping("/category/{categoryId}")
    public Result getSetmealById(@PathVariable("categoryId") Long categoryId) {
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<Setmeal>();
        wrapper.eq(Setmeal::getCategoryId, categoryId);
        List<Setmeal> setmealList = setmealService.list(wrapper);
        return Result.success(setmealList);
    }
    @GetMapping("/{setmealId}")
    public Result getSetmealDishById(@PathVariable("setmealId") Long setmealId) {
        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<SetmealDish>();
        wrapper.eq(SetmealDish::getSetmealId, setmealId);
        List<SetmealDish> setmealDishList = setmealDishService.list(wrapper);
        return Result.success(setmealDishList);
    }
}
