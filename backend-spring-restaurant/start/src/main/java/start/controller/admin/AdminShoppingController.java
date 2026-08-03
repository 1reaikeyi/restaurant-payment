package start.controller.admin;

import com.alipay.api.domain.Shop;
import common.constant.ShopConstant;
import common.enumOperation.OperationEnum;
import common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import start.aop.OperationLogging;

@RestController
@RequestMapping("/admin/shop")
public class AdminShoppingController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @OperationLogging(operation = OperationEnum.CREATE)
    @PostMapping("{status}")
    public Result updateStatus(@PathVariable Long status) {
        stringRedisTemplate.opsForValue().set(ShopConstant.SHOP_STATUS, status == 1 ? "营业中" : "已打烊");
        return Result.success(OperationEnum.CREATE+(status == 1 ? "营业中" : "已打烊"));
    }

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
