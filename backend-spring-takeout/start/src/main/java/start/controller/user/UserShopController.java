package start.controller.user;

import common.constant.ShopConstant;
import common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/shop")
public class UserShopController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/status")
    public Result getStatus() {
        String status = stringRedisTemplate.opsForValue().get(ShopConstant.SHOP_STATUS);
        return Result.success("getStatus::"+status);
    }
}