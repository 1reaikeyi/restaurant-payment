package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mapper.DishDetailMapper;
import org.springframework.stereotype.Service;
import model.entity.DishDetail;
import service.DishDetailService;

/**
 * 菜品口味关系 Service（对应 dish_detail 表）
 */
@Service
public class DishDetailServiceImpl extends ServiceImpl<DishDetailMapper, DishDetail> implements DishDetailService {
}
