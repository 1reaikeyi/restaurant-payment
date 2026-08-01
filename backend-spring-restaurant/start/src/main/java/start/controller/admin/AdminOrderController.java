//package start.controller.admin;
//
//import com.alipay.api.AlipayApiException;
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.baomidou.mybatisplus.core.metadata.IPage;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import common.result.Result;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//import pojo.dto.order.OrderStatisticsDTO;
//import pojo.dto.order.OrdersPageDTO;
//import pojo.entity.Orders;
//import pojo.entityenum.DeliveryStatusEnum;
//import pojo.entityenum.OrderStatusEnum;
//import service.ISevcive.OrderService;
//import start.controller.pay.service.AlipayService;
//import start.controller.pay.DTO.RefundDTO;
//
//import java.time.LocalDateTime;
//
//@RestController
//@RequestMapping("/admin/order")
//public class AdminOrderController {
//
//    @Autowired
//    private OrderService orderService;
//    @Autowired
//    private AlipayService alipayService;
//
//    @GetMapping("/all")
//    public Result readAllOrders() {
//        return Result.success(orderService.list(new LambdaQueryWrapper<Orders>()
//                .orderByDesc(Orders::getOrderTime)
//                .last("limit 20")));
//    }
//    /**
//     * 获取订单详情（包含订单明细）
//     * @param id 订单ID
//     * @return 订单详情数据
//     */
//    @GetMapping("get/{id}")
//    public Result getOrder(@PathVariable Long id) {
//        return Result.success(orderService.getOrderWithDetails(id));
//    }
//    @GetMapping("/conditionSearch")
//    public Result conditionSearch(OrdersPageDTO ordersPageDTO) {
//        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper = new LambdaQueryWrapper<>();
//        ordersLambdaQueryWrapper.eq(Orders::getPhone , ordersPageDTO.getPhone());
//        IPage<Orders> ordersIPage = new Page<>(ordersPageDTO.getPage(), ordersPageDTO.getPageSize());
//        IPage<Orders> ordersResult = orderService.page(ordersIPage, ordersLambdaQueryWrapper);
//        return Result.success(ordersResult);
//    }
//    /**
//     * 获取订单统计信息
//     * 返回所有订单状态的统计数量
//     * @return 订单统计数据
//     */
//    @GetMapping("/statistics")
//    public Result statistics() {
//        OrderStatisticsDTO orderStatisticsDTO = new OrderStatisticsDTO();
//
//        // "待支付：下单未付款"
//        LambdaQueryWrapper<Orders> qw1 = new LambdaQueryWrapper<>();
//        qw1.eq(Orders::getStatus, OrderStatusEnum.PENDING_PAYMENT);
//        orderStatisticsDTO.setPendingPayment(orderService.count(qw1));
//
//        // "待商家接单：已付款，商家还没接单"
//        LambdaQueryWrapper<Orders> qw2 = new LambdaQueryWrapper<>();
//        qw2.eq(Orders::getStatus, OrderStatusEnum.PENDING_MERCHANT_ACCEPT);
//        orderStatisticsDTO.setToBeConfirmed(orderService.count(qw2));
//
//        // 商家接单,制作中：商家确认接单，正在做菜
//        LambdaQueryWrapper<Orders> qw3 = new LambdaQueryWrapper<>();
//        qw3.eq(Orders::getStatus, OrderStatusEnum.MERCHANT_COOKING);
//        orderStatisticsDTO.setMerchantCooking(orderService.count(qw3));
//
//        // 待骑手取餐：商家出餐完成，骑手还没到店
//        LambdaQueryWrapper<Orders> qw4 = new LambdaQueryWrapper<>();
//        qw4.eq(Orders::getStatus, OrderStatusEnum.PENDING_RIDER_PICK);
//        orderStatisticsDTO.setPendingRiderPick(orderService.count(qw4));
//
//        // 骑手已取餐，配送中：骑手拿到餐，在路上，实时看定位
//        LambdaQueryWrapper<Orders> qw5 = new LambdaQueryWrapper<>();
//        qw5.eq(Orders::getStatus, OrderStatusEnum.RIDER_DELIVERING);
//        orderStatisticsDTO.setRiderDelivering(orderService.count(qw5));
//
//        // 骑手已送达：骑手点送达，等待用户确认
//        LambdaQueryWrapper<Orders> qw6 = new LambdaQueryWrapper<>();
//        qw6.eq(Orders::getStatus, OrderStatusEnum.RIDER_ARRIVED);
//        orderStatisticsDTO.setRiderArrived(orderService.count(qw6));
//
//        // "订单已完成：系统自动确认收货"
//        LambdaQueryWrapper<Orders> qw7 = new LambdaQueryWrapper<>();
//        qw7.eq(Orders::getStatus, OrderStatusEnum.COMPLETED);
//        orderStatisticsDTO.setCompleted(orderService.count(qw7));
//
//        // "订单已取消：未接单退款、商家拒单、超时取消、售后全额退款"
//        LambdaQueryWrapper<Orders> qw8 = new LambdaQueryWrapper<>();
//        qw8.eq(Orders::getStatus, OrderStatusEnum.CANCELLED);
//        orderStatisticsDTO.setCancelled(orderService.count(qw8));
//
//        return Result.success(orderStatisticsDTO);
//    }
//
//    /**
//     * 商家接受订单，开始制作
//     * @return
//     */
//    @PutMapping("/confirm/{orderId}")
//    public Result confirmOrders(@PathVariable Long orderId) {
//        Orders orders = orderService.getById(orderId);
//        orders.setStatus(OrderStatusEnum.MERCHANT_COOKING);
//        if (orders.getDeliveryStatusEnum() == DeliveryStatusEnum.NOW) {
//            // 核心：当前时间 + 30分钟
//            orders.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(60));
//        }
//        orderService.updateById(orders);
//        return Result.success(orderId);
//    }
//    /**
//     * 等待骑手开始接单
//     * @return
//     */
//    @PutMapping("/begin/{id}")
//    public Result beginOrders(@PathVariable Long id) {
//        Orders orders = orderService.getById(id);
//        orders.setStatus(OrderStatusEnum.PENDING_RIDER_PICK);
//        orders.setStartDeliveryTime(LocalDateTime.now());
//        orders.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(60));
//        // 修复：保存订单更新到数据库
//        orderService.updateById(orders);
//        return Result.success(id);
//    }
//
//    /**
//     * 开始派送
//     * @return
//     */
//    @PutMapping("/delivery/{id}")
//    public Result deliveryOrders(@PathVariable Long id) {
//        Orders orders = orderService.getById(id);
//        orders.setStatus(OrderStatusEnum.RIDER_DELIVERING);
//        orders.setStartDeliveryTime(LocalDateTime.now());
//        orders.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(60));
//        // 修复：保存订单更新到数据库
//        orderService.updateById(orders);
//        return Result.success(id);
//    }
//
//    /**
//     * 已完成派送
//     * @return
//     */
//    @PutMapping("/complete/{id}")
//    public Result completeOrders(@PathVariable Long id) {
//        Orders orders = orderService.getById(id);
//        orders.setStatus(OrderStatusEnum.COMPLETED);
//        orders.setDeliveryTime(LocalDateTime.now());
//        return Result.success(id);
//    }
//    /**
//     * 商家取消订单，用户退款订单
//     * out_trade_no
//     * refund_amount
//     * out_request_no
//     */
//    @PutMapping("/cancel/{id}")
//    public Result cancelOrder(@PathVariable Long id) {
//        Orders orders = orderService.getById(id);
//        orders.setStatus(OrderStatusEnum.CANCELLED);
//        orders.setCancelReason("XXX");
//        orderService.updateById(orders);
//        try {
//            String out_request_no = LocalDateTime.now().toString();
//            RefundDTO refundDTO = new RefundDTO(orders.getId().toString(),
//                    orders.getAmount(),
//                    out_request_no,
//                    orders.getCancelReason());
//            alipayService.refund(refundDTO);
//        } catch (AlipayApiException e) {
//            throw new RuntimeException(e);
//        }
//        return Result.success("cancelOrder"+ id);
//    }
//
//}
