package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mapper.RestaurantCategoryMapper;
import org.springframework.stereotype.Service;
import model.entity.RestaurantCategory;
import service.RestaurantCategoryService;

/**
 * 餐厅分类 Service（对应 restaurant_category 表）
 */
@Service
public class RestaurantCategoryServiceImpl extends ServiceImpl<RestaurantCategoryMapper, RestaurantCategory> implements RestaurantCategoryService {
}
