package start.controller.user;

import com.alipay.api.response.AlipayTradeRefundResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.enumOperation.OperationEnum;
import common.result.Result;
import jakarta.servlet.http.HttpServletResponse;
import model.dto.OrderPageDTO;
import model.entity.Order;
import model.entity.OrderDetail;
import model.entity.OrderPay;
import model.entityenum.OrderStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import service.OrderDetailService;
import service.OrderService;
import service.PayService;
import start.aop.OperationLogging;
import start.controller.支付宝.DTO.PayDTO;
import start.controller.支付宝.DTO.RefundDTO;
import start.controller.支付宝.service.ZhifubaoService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/user/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ZhifubaoService zhifubaoService;
    @Autowired
    private PayService payService;

    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping
    public Result readByOrderId(@RequestParam("id") Long id) {
        Order order = orderService.lambdaQuery().eq(Order::getId, id).one();
        List<OrderDetail> orderDetails = orderDetailService.lambdaQuery()
                .in(OrderDetail::getOrderId, id).list();
        return Result.success(order+"::"+orderDetails);
    }
    @OperationLogging(operation = OperationEnum.READ)
    @GetMapping("/all")
    public Result readAll(OrderPageDTO orderPageDTO) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(orderPageDTO.getStatus()!= null,Order::getStatus,orderPageDTO.getStatus());
        IPage<Order> page = new Page<>(orderPageDTO.getPage(), orderPageDTO.getPageSize());
        IPage<Order> orderIPage = orderService.page(page, queryWrapper);
        return Result.success(orderIPage);
    }
    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping("cooking/{id}")
    public Result update1(@PathVariable Long id) {
        Order order = orderService.lambdaQuery().eq(Order::getId, id).one();
        order.setStatus(OrderStatusEnum.MERCHANT_COOKING);
        return Result.success(order.getStatus());
    }
    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping("pending/{id}")
    public Result update2(@PathVariable Long id) {
        Order order = orderService.lambdaQuery().eq(Order::getId, id).one();
        order.setStatus(OrderStatusEnum.PENDING_RIDER_PICK);
        return Result.success(order.getStatus());
    }
    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping("/complete/{id}")
    public Result update7(@PathVariable Long id) {
        Order order = orderService.lambdaQuery().eq(Order::getId, id).one();
        order.setStatus(OrderStatusEnum.COMPLETED);
        return Result.success();
    }
    @Transactional(rollbackFor = Exception.class)
    @OperationLogging(operation = OperationEnum.UPDATE)
    @PutMapping("canceled/{id}")
    public Result update8(@PathVariable Long id) {
        Order order = orderService.lambdaQuery().eq(Order::getId, id).one();
        OrderPay orderPay = payService.lambdaQuery().eq(OrderPay::getOrderId, id).one();
        order.setStatus(OrderStatusEnum.CANCELLED);

        RefundDTO refundDTO = new RefundDTO();
        LocalDateTime now = LocalDateTime.now();
        String timeString = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        refundDTO.setRefundAmount(orderPay.getAmount());
        refundDTO.setOutTradeNo(timeString);
        refundDTO.setOutRefundNo(timeString);
        refundDTO.setRefundReason("XXXXXXXXXXXXXXXXXXXXXXX");

        orderPay.setRemark(OrderStatusEnum.CANCELLED.getFullText());
        orderPay.setRejectionReason("商家主动取消订单");

        try {
            refund(refundDTO);
            payService.updateById(orderPay);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Result.success(order.getStatus());
    }
    public AlipayTradeRefundResponse refund(RefundDTO refundDTO) throws Exception {
        return zhifubaoService.refund(refundDTO);
    }
    public void orderPay(PayDTO payDTO, HttpServletResponse response) throws Exception {
        String form = zhifubaoService.createPagePayForm(payDTO);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(form);
        response.getWriter().flush();
    }
}
