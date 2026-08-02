package start.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.DishDetailService;
import service.DishService;

@RestController
@RequestMapping("/admin/dish")
public class AdminDishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishDetailService dishDetailService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
}
