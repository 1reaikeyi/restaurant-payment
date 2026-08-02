package start.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.DishDetailService;
import service.DishService;
@RestController
@RequestMapping("/user/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishDetailService dishDetailService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
}
