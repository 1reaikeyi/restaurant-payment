package start.controller.pay;

import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import start.controller.pay.DTO.PayDTO;
import start.controller.pay.DTO.RefundDTO;
import start.controller.pay.service.AlipayService;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/pay")
@Slf4j
public class AlipayController {

    @Autowired
    private AlipayService alipayService;

    /** 电脑网站支付：浏览器打开此接口会跳转到支付宝沙箱收银台 */
    @GetMapping("/order")
    public void orderPay(PayDTO payDTO, HttpServletResponse response) throws Exception {
        String form = alipayService.createPagePayForm(payDTO);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(form);
        response.getWriter().flush();
    }
    /** 交易查询 */
    @GetMapping("/order/query")
    public AlipayTradeQueryResponse queryOrder(@RequestParam String outTradeNo) throws Exception {
        return alipayService.queryTrade(outTradeNo);
    }
    /** 退款 */
    @PostMapping("/refund")
    public AlipayTradeRefundResponse refundOrder(RefundDTO refundDTO) throws Exception {
        return alipayService.refund(refundDTO);
    }

    /** 退款查询 */
    @GetMapping("/refund/query")
    public AlipayTradeFastpayRefundQueryResponse refundQuery(@RequestParam String outTradeNo, @RequestParam String outRequestNo) throws Exception {
        return alipayService.refundQuery(outTradeNo, outRequestNo);
    }

    /** 关闭交易 */
    @PostMapping("/order/close")
    public AlipayTradeCloseResponse close(@RequestParam String outTradeNo) throws Exception {
        return alipayService.close(outTradeNo);
    }

    /** 同步跳转 */
    @GetMapping("/return")
    public String returnUrl() {
        return "已返回商户页面,同步返回。";
    }

    /** 异步通知：必须公网可访问（本地可用内网穿透） */
//    @PostMapping("/notify")
//    public String notifyUrl(HttpServletRequest request) {
//        log.info("收到支付宝异步通知");
//        Map<String, String> params = new HashMap<>();
//        // 1. 转换请求参数
//        request.getParameterMap().forEach((key, values) -> {
//            if (values != null && values.length > 0) {
//                params.put(key, values[0]);
//            }
//        });
//        try {
//            // 2. 验签 (核心安全步骤)
//            boolean signVerified = AlipaySignature.rsaCheckV1(
//                    params,
//                    alipayConfig.getAlipayPublicKey(),
//                    alipayConfig.getCharset(),
//                    alipayConfig.getSignType()
//            );
//
//            if (!signVerified) {
//                log.error("支付宝异步通知验签失败！疑似非法请求。params={}", params);
//                return "failure";
//            }
//
//            // 3. 基础字段校验
//            String appId = params.get("app_id");
//            String outTradeNo = params.get("out_trade_no"); // 商户订单号
//            String tradeNo = params.get("trade_no");        // 支付宝交易号
//            String tradeStatus = params.get("trade_status");
//            String totalAmount = params.get("total_amount"); // 订单金额

//            // 校验 app_id 是否匹配
//            if (appId == null || !appId.equals(alipayConfig.getAppId())) {
//                log.error("通知 app_id 不匹配，预期: {}, 实际: {}", alipayConfig.getAppId(), appId);
//                return "failure";
//            }
//
//            // 4. 业务逻辑处理
//            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
//
//                // 先查询数据库，看该订单是否已经处理过
//                Orders order = orderService.getById(outTradeNo);
//                if (order == null) {
//                    log.error("订单不存在！out_trade_no={}", outTradeNo);
//                    return "failure";
//                }
//
//                // 如果订单状态已经是“已支付”，说明之前已经处理过了，直接返回 success
//                if (OrderStatusEnum.PAID.equals(order.getStatus())) {
//                    log.info("订单已处理过，跳过幂等检查。out_trade_no={}", outTradeNo);
//                    return "success";
//                }
//
//                // --- 步骤 B: 金额校验 (防止篡改金额) ---
//                // 使用 BigDecimal 进行精确比较
//                java.math.BigDecimal notifyAmount = new java.math.BigDecimal(totalAmount);
//                if (notifyAmount.compareTo(order.getAmount()) != 0) {
//                    log.error("订单金额不一致！out_trade_no={}, 通知金额={}, 数据库金额={}", outTradeNo, notifyAmount, order.getTotalAmount());
//                    return "failure";
//                }
//                // 建议开启数据库事务，确保状态更新和后续逻辑的一致性
//                orderService.updateOrderStatus(outTradeNo, OrderStatusEnum.PAID, tradeNo);
//
                // 这里可以发送消息到 MQ 处理非核心逻辑（如发短信、加积分），避免阻塞回调
                // mqProducer.sendOrderPaidMessage(outTradeNo);
//
//                log.info("支付宝异步通知处理成功，订单号: {}, 交易号: {}", outTradeNo, tradeNo);
//                return "success";
//            }

//            // 处理其他状态（如交易关闭）
//            log.info("收到非成功状态通知: {}, 订单号: {}", tradeStatus, outTradeNo);
//            return "success";
//        } catch (Exception e) {
//            log.error("处理支付宝异步通知发生异常", e);
//            // 捕获异常返回 failure，让支付宝重试，避免丢失通知
//            return "failure";
//        }
//    }

}
