package start.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import common.constant.JwtClaimsConstant;
import common.localContextHolder.ThreadLocalContextHolder;
import common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pojo.dto.shopping.ShoppingCartDTO;
import pojo.entity.Dish;
import pojo.entity.Setmeal;
import pojo.entity.ShoppingCart;
import start.annotation.Info;
import service.ISevcive.DishService;
import service.ISevcive.SetmealService;
import service.ISevcive.ShoppingCartService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/shoppingCarts")
public class UserShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    @PostMapping
    @Info(desc = "添加购物车")
    public Result createShoppingCart(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        //新建购物车
        ShoppingCart shoppingCart = new ShoppingCart();
        Map<String, Object> claims = ThreadLocalContextHolder.get();
        String currentId = claims.get(JwtClaimsConstant.USER_ID).toString();
        Long userId = Long.parseLong(currentId);
        shoppingCart.setUserId(userId);
        //2判断是菜品还是套餐，根据dishId和setmealId判断
        //菜品
        if (shoppingCartDTO.getSetmealId() == null) {
            shoppingCart.setSetmealId(0L);
            //获取菜品价格
            Dish dish = dishService.getById(shoppingCartDTO.getDishId());
            BigDecimal dishPrice = dish.getPrice();
            shoppingCart.setDishId(shoppingCartDTO.getDishId());
            shoppingCart.setDishFlavor(shoppingCartDTO.getDishFlavor());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setName(dish.getName());
            // 只匹配“同用户 + 同菜品 + 同口味”的一条记录
            LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ShoppingCart::getUserId, userId)
                    .eq(ShoppingCart::getDishId, shoppingCartDTO.getDishId())
                    .eq(ShoppingCart::getDishFlavor, shoppingCartDTO.getDishFlavor());
            ShoppingCart same_shoppingCart = shoppingCartService.getOne(wrapper);
            if (same_shoppingCart != null) {
                //如果存在，更新数量和金额
                Long newNumber = same_shoppingCart.getNumber() + shoppingCartDTO.getNumber();
                BigDecimal newAmount = same_shoppingCart.getAmount().add(dishPrice.multiply(BigDecimal.valueOf(shoppingCartDTO.getNumber())));
                same_shoppingCart.setNumber(newNumber);
                same_shoppingCart.setAmount(newAmount);
                shoppingCartService.updateById(same_shoppingCart);
            }
            if (same_shoppingCart == null) {
                //如果不存在，新增购物车
                shoppingCart.setNumber(shoppingCartDTO.getNumber());
                shoppingCart.setAmount(dishPrice.multiply(BigDecimal.valueOf(shoppingCartDTO.getNumber())));
                shoppingCartService.save(shoppingCart);
            }
            return Result.success("createShoppingCart::"+shoppingCart.getId());
        }

        //套餐
        if (shoppingCartDTO.getSetmealId() != null) {
            shoppingCart.setDishId(0L);
            //获取套餐价格
            Setmeal setmeal = setmealService.getById(shoppingCartDTO.getSetmealId());
            BigDecimal setmealPrice = setmeal.getPrice();
            shoppingCart.setSetmealId(shoppingCartDTO.getSetmealId());
            shoppingCart.setDishFlavor(shoppingCartDTO.getDishFlavor());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setName(setmeal.getName());

            // 只匹配“同用户 + 同套餐 + 同口味(可为空)”的一条记录
            LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ShoppingCart::getUserId, userId)
                    .eq(ShoppingCart::getSetmealId, shoppingCartDTO.getSetmealId())
                    .eq(ShoppingCart::getDishFlavor, shoppingCartDTO.getDishFlavor());
            ShoppingCart same_shoppingCart = shoppingCartService.getOne(wrapper);
            if (same_shoppingCart != null) {
                //如果存在，更新数量和金额
                Long newNumber = same_shoppingCart.getNumber() + shoppingCartDTO.getNumber();
                BigDecimal newAmount = same_shoppingCart.getAmount().add(setmealPrice.multiply(BigDecimal.valueOf(shoppingCartDTO.getNumber())));
                same_shoppingCart.setNumber(newNumber);
                same_shoppingCart.setAmount(newAmount);
                shoppingCartService.updateById(same_shoppingCart);
            }
            if (same_shoppingCart == null) {
                //如果不存在，新增购物车
                shoppingCart.setNumber(shoppingCartDTO.getNumber());
                shoppingCart.setAmount(setmealPrice.multiply(BigDecimal.valueOf(shoppingCartDTO.getNumber())));
                shoppingCartService.save(shoppingCart);
            }
            return Result.success("createShoppingCart::"+shoppingCart.getId());
        }
        return Result.error("添加失败");
    }
    @GetMapping
    public Result readShoppingCart() {
        Map<String, Object> claims = ThreadLocalContextHolder.get();
        String currentId = claims.get(JwtClaimsConstant.USER_ID).toString();
        Long userId = Long.parseLong(currentId);
        LambdaQueryWrapper<ShoppingCart> Wrapper = new LambdaQueryWrapper<>();
        Wrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(Wrapper);
        return Result.success(shoppingCartList);
    }
    @DeleteMapping("/{id}")
    public Result deleteShoppingCart(@PathVariable Long id) {
        shoppingCartService.removeById(id);
        return Result.success("deleteShoppingCart::"+id);
    }
    @DeleteMapping("/all")
    public Result clean() {
        Map<String, Object> map = ThreadLocalContextHolder.get();
        String currentId = map.get(JwtClaimsConstant.USER_ID).toString();
        Long userId = Long.parseLong(currentId);
        LambdaQueryWrapper<ShoppingCart> Wrapper = new LambdaQueryWrapper<>();
        Wrapper.eq(ShoppingCart::getUserId, userId);
        shoppingCartService.remove(Wrapper);
        return Result.success("clean");
    }
}
