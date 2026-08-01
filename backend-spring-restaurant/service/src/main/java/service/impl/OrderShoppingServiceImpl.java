package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mapper.OrderShoppingMapper;
import org.springframework.stereotype.Service;
import model.entity.OrderShopping;
import service.OrderShoppingService;

/**
 * 购物车 Service（对应 order_shopping 表）
 */
@Service
public class OrderShoppingServiceImpl extends ServiceImpl<OrderShoppingMapper, OrderShopping> implements OrderShoppingService {
}
