package service.ISevcive;

import com.baomidou.mybatisplus.extension.service.IService;
import pojo.entity.Orders;

import java.util.Map;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Orders> {

    /**
     * 获取订单详情（包含订单明细列表）
     * @param id 订单ID
     * @return 包含订单主信息和订单明细列表的Map
     */
    Map<String, Object> getOrderWithDetails(Long id);
}
