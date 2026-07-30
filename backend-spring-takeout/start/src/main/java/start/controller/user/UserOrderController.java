package start.controller.user;

import cn.hutool.core.bean.BeanUtil;
import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.constant.JwtClaimsConstant;
import common.localContextHolder.ThreadLocalContextHolder;
import common.result.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pojo.dto.order.OrdersDTO;
import pojo.dto.order.OrdersHistoryDTO;
import pojo.entity.OrderDetail;
import pojo.entity.Orders;
import pojo.entityenum.OrderStatus;
import service.ISevcive.OrderDetailService;
import service.ISevcive.OrderService;
import start.controller.pay.service.AlipayService;
import start.controller.pay.DTO.PayDTO;
import start.controller.pay.DTO.RefundDTO;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/orders")
@Transactional(rollbackFor = Exception.class)
public class UserOrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private AlipayService alipayService;

    /**
     *下单支付
     * 业务参数
     * out_trade_no：商户订单号
     * total_amount：订单金额（单位：元）
     * subject：订单名称
     */
    @GetMapping("/pay/{id}")
    public void payOrder(@PathVariable Long id, HttpServletResponse response) throws Exception {
        Orders payOrder = orderService.getById(id);
        payOrder.setStatus(OrderStatus.ACCEPTED);
        //1->支付宝，2是微信
        payOrder.setPayMethod(1L);
        orderService.updateById(payOrder);
        PayDTO payDTO = new PayDTO(payOrder.getId().toString(),payOrder.getAmount(),payOrder.getConsignee());
        String form = alipayService.createPagePayForm(payDTO);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(form);
        response.getWriter().flush();
    }
    /**
     * 退款
     * out_trade_no
     * refund_amount
     * out_request_no
     */
    @PutMapping("/cancel/{id}")
    public Result cancelOrder(@PathVariable Long id) {
        Orders orders = orderService.getById(id);
        orders.setStatus(OrderStatus.CANCELLED);
        orders.setCancelReason("XXXXXXXXXX");
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
    @PostMapping("/submit")
    public Result submitOrder(@RequestBody OrdersDTO orderDTO) {
        Orders orders = BeanUtil.toBean(orderDTO, Orders.class);
        orders.setStatus(OrderStatus.PENDING);
        orderService.save(orders);
        Long orderId = orders.getId();
        List<OrderDetail> orderDetailList = orderDTO.getOrderDetails()
                    .stream()
                    .map((OrderDetail orderDetail) -> {
                        OrderDetail orderDetail1 = BeanUtil.toBean(orderDetail, OrderDetail.class);
                       
                        orderDetail1.setOrderId(orderId);
                        return orderDetail1;
                    })
                    .toList();
        orderDetailService.saveBatch(orderDetailList);
        return Result.success("submitOrder::" + orders.getId());
    }


    @PostMapping("/repetition/{id}")
    public Result repetitionOrder(@PathVariable Long id, HttpServletResponse httpServletResponse) {
        Orders oldOrders = orderService.getById(id);
        if (oldOrders == null) {
            return Result.error("订单不存在");
        }
        Orders newOrders = BeanUtil.toBean(oldOrders, Orders.class);
        orderService.save(newOrders);
        List<OrderDetail> orderDetailList = orderDetailService.list(new LambdaQueryWrapper<OrderDetail>()
                .eq(OrderDetail::getOrderId, oldOrders.getId()));
        orderDetailService.saveBatch(orderDetailList);
        return Result.success("repetitionOrder::" + newOrders.getId());
    }
    @GetMapping("/get/{id}")
    public Result readOrderById(@PathVariable Long id) {
        Map<String, Object> map = ThreadLocalContextHolder.get();
        String currentUserId = map.get(JwtClaimsConstant.USER_ID).toString();
        Long userId = Long.parseLong(currentUserId);
        Orders orders = orderService.getOne(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getId, id)
                .eq(Orders::getUserId, userId));
        if (orders == null) {
            return Result.error("订单不存在");
        }
        return Result.success(orders);
    }
    @GetMapping("/history")
    public Result historyOrder(OrdersHistoryDTO ordersHistoryDTO) {
        Map<String, Object> claims = ThreadLocalContextHolder.get();
        String currentId = claims.get(JwtClaimsConstant.USER_ID).toString();
        Long userId = Long.parseLong(currentId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startOfDay = LocalDateTime.parse(ordersHistoryDTO.getStartTime() + " 00:00:00", formatter);
        LocalDateTime endOfDay = LocalDateTime.parse(ordersHistoryDTO.getEndTime() + " 23:59:59", formatter);
        LambdaQueryWrapper<Orders> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Orders::getUserId, userId)
                .between(Orders::getOrderTime, startOfDay,endOfDay);
        IPage<Orders> ordersIPage = new Page<>(ordersHistoryDTO.getPage(), ordersHistoryDTO.getPageSize());
        IPage<Orders> page = orderService.page(ordersIPage, lambdaQueryWrapper);
        return Result.success(page);
    }
    @PostMapping("/reminder/{id}")
    public Result reminderOrder(@PathVariable Long id) {
        return Result.success("订单"+id+"需要加急处理");
    }

}
