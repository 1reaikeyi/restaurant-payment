package service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pojo.entity.OrderDetail;
import pojo.entity.Orders;
import repository.OrderDetailMapper;
import repository.OrderMapper;
import service.ISevcive.OrderService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单服务实现类
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    /**
     * 获取订单详情（包含订单明细列表）
     * @param id 订单ID
     * @return 包含订单主信息和订单明细列表的Map
     */
    @Override
    public Map<String, Object> getOrderWithDetails(Long id) {
        Map<String, Object> result = new HashMap<>();
        
        // 查询订单主信息
        Orders order = this.getById(id);
        if (order == null) {
            return result;
        }
        result.put("order", order);
        
        // 查询订单明细列表
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, id);
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(queryWrapper);
        result.put("orderDetailList", orderDetailList);
        
        return result;
    }
}
