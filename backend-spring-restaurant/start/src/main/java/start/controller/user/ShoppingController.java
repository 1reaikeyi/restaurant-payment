package start.controller.user;

import common.constant.ShopConstant;
import common.enumOperation.OperationEnum;
import common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import start.aop.OperationLogging;

@RestController
@RequestMapping("/user/shop")
public class ShoppingController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping
    public Result read() {
        String shopping = stringRedisTemplate.opsForValue().get(ShopConstant.SHOP_STATUS);
        if (shopping == null) {
            shopping = "已打烊";
            return Result.success(OperationEnum.READ+"--"+shopping);
        }
        return Result.success(OperationEnum.READ+"--"+shopping);
    }
}
