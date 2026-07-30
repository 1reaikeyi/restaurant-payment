package start.controller.admin;

import common.constant.ShopConstant;
import common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/shop")
public class AdminShopStatusController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/status")
    public Result getStatus() {
        String status = stringRedisTemplate.opsForValue().get(ShopConstant.SHOP_STATUS);
        return Result.success(status);
    }
    @PutMapping("/status/{status}")
    public Result updateStatus(@PathVariable Long status) {
        stringRedisTemplate.opsForValue().set(ShopConstant.SHOP_STATUS, status == 1 ? "营业中" : "已打烊");
        return Result.success("updateStatus::"+(status == 1 ? "营业中" : "已打烊"));
    }
}
