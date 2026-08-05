package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mapper.OrderPayMapper;
import org.springframework.stereotype.Service;
import model.entity.OrderPay;
import service.PayService;

/**
 * 订单支付 Service（对应 order_pay 表）
 */
@Service
public class PayServiceImpl extends ServiceImpl<OrderPayMapper, OrderPay> implements PayService {
}
