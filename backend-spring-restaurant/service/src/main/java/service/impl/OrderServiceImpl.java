package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mapper.OrderMapper;
import org.springframework.stereotype.Service;
import model.entity.Order;
import service.OrderService;

/**
 * 订单 Service（对应 order 表）
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
}
