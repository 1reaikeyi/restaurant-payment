package service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import mapper.OrderDetailMapper;
import org.springframework.stereotype.Service;
import model.entity.OrderDetail;
import service.OrderDetailService;

/**
 * 订单详情 Service（对应 order_detail 表）
 */
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
