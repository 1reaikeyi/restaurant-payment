package start.controller.admin;

import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pojo.dto.order.OrderStatisticsDTO;
import pojo.dto.order.OrdersPageDTO;
import pojo.entity.Orders;
import pojo.entityenum.DeliveryStatus;
import pojo.entityenum.OrderStatus;
import service.ISevcive.OrderService;
import start.controller.pay.service.AlipayService;
import start.controller.pay.DTO.RefundDTO;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private AlipayService alipayService;

    @GetMapping("/all")
    public Result readAllOrders() {
        return Result.success(orderService.list(new LambdaQueryWrapper<Orders>()
                .orderByDesc(Orders::getOrderTime)
                .last("limit 20")));
    }
    @GetMapping("get/{id}")
    public Result getOrder(@PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }
    @GetMapping("/statistics")
    public Result statistics() {
        OrderStatisticsDTO orderStatisticsDTO = new OrderStatisticsDTO();
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper1.eq(Orders::getStatus , OrderStatus.ACCEPTED);
        orderStatisticsDTO.setToBeConfirmed(orderService.count(ordersLambdaQueryWrapper1));
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper2 = new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper2.eq(Orders::getStatus ,OrderStatus.DELIVERING);
        orderStatisticsDTO.setDeliveryInProgress(orderService.count(ordersLambdaQueryWrapper2));
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper3 = new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper3.eq(Orders::getStatus ,OrderStatus.COMFIRM);
        orderStatisticsDTO.setDeliveryInProgress(orderService.count(ordersLambdaQueryWrapper3));
        return Result.success(orderStatisticsDTO);
    }
    @GetMapping("/conditionSearch")
    public Result conditionSearch(OrdersPageDTO ordersPageDTO) {
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper = new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper.eq(Orders::getPhone , ordersPageDTO.getPhone());
        IPage<Orders> ordersIPage = new Page<>(ordersPageDTO.getPage(), ordersPageDTO.getPageSize());
        IPage<Orders> ordersResult = orderService.page(ordersIPage, ordersLambdaQueryWrapper);
        return Result.success(ordersResult);
    }

    /**
     * 商家接受订单，开始制作
     * @return
     */
    @PutMapping("/confirm/{orderId}")
    public Result confirmOrders(@PathVariable Long orderId) {
        Orders orders = orderService.getById(orderId);
        orders.setStatus(OrderStatus.COMFIRM);
        if (orders.getDeliveryStatus() == DeliveryStatus.NOW) {
            // 核心：当前时间 + 30分钟
            orders.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(60));
        }
        orderService.updateById(orders);
        return Result.success(orderId + "::" + OrderStatus.COMFIRM);
    }
    /**
     * 商家取消订单，用户退款订单
     * out_trade_no
     * refund_amount
     * out_request_no
     */
    @PutMapping("/cancel/{id}")
    public Result cancelOrder(@PathVariable Long id) {
        Orders orders = orderService.getById(id);
        orders.setStatus(OrderStatus.CANCELLED);
        orders.setCancelReason("XXX");
        orderService.updateById(orders);
        try {
            String out_request_no = LocalDateTime.now().toString();
            RefundDTO refundDTO = new RefundDTO(orders.getId().toString(),
                    orders.getAmount(),
                    out_request_no,
                    orders.getCancelReason());
            alipayService.refund(refundDTO);
        } catch (AlipayApiException e) {
            throw new RuntimeException(e);
        }
        return Result.success("cancelOrder"+ id);
    }

    /**
     * 开始派送
     * @return
     */
    @PutMapping("/delivery/{id}")
    public Result deliveryOrders(@PathVariable Long id) {
        Orders orders = orderService.getById(id);
        orders.setStatus(OrderStatus.DELIVERING);
        orders.setStartDeliveryTime(LocalDateTime.now());
        orders.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(60));
        // 修复：保存订单更新到数据库
        orderService.updateById(orders);
        return Result.success(id + "::" + OrderStatus.DELIVERING);
    }

    /**
     * 已完成派送
     * @return
     */
    @PutMapping("/complete/{id}")
    public Result completeOrders(@PathVariable Long id) {
        Orders orders = orderService.getById(id);
        orders.setStatus(OrderStatus.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        return Result.success(id + "::" + OrderStatus.COMPLETED);
    }


}
