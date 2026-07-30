package start.controller.timetask;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pojo.entity.Orders;
import pojo.entityenum.DeliveryStatusEnum;
import pojo.entityenum.OrderStatusEnum;
import service.ISevcive.OrderService;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderService orderService;
    /**
     * 帮助处理订单
     */
//    @Scheduled 使用 6 字段 cron 表达式（秒 分 时 日 月 周）
//  1	秒	0-59	*任意 / */n每隔 n 秒
//  2	分	0-59	*任意 / */n每隔 n 分钟
//  3	时	0-23	*任意小时
//  4	日	1-31	*任意日期
//  5	月	1-12	*任意月份
//  6	周	1 (周日)-7 (周六)	? 不指定（和日互斥）
    //每60分钟触发一次
    @Scheduled(cron = "0 0 * * * ?")
    public void processTimeoutOrder(){
        LambdaQueryWrapper<Orders> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(Orders::getStatus, OrderStatusEnum.PENDING_RIDER_PICK);
        List<Orders> ordersList1 = orderService.list(wrapper1);
        if (!ordersList1.isEmpty()) {
            log.info("有待处理订单：{}", ordersList1);
        }
        LambdaQueryWrapper<Orders> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(Orders::getDeliveryStatusEnum, DeliveryStatusEnum.NOW);
        List<Orders> ordersList2 = orderService.list(wrapper2);
        for (Orders orders : ordersList2) {
            LocalDateTime now = LocalDateTime.now();
            if (orders.getStartDeliveryTime() != null && now.isAfter(orders.getStartDeliveryTime())) {
                    log.info("id为{}订单需要开始派送了", orders.getId());
            }
        }

    }

}
