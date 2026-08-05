package model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.entityenum.PayStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPayDTO {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 关联orderId
     */
    private Long orderId;

    /**
     * 下单时间
     */
    private LocalDateTime orderTime;

    /**
     * 支付方式 1微信,2支付宝
     */
    private Long payMethod;

    /**
     * 支付状态 0未支付 1已支付 2退款
     */
    private PayStatusEnum payStatus;

    /**
     * 结账时间
     */
    private LocalDateTime checkoutTime;

    /**
     * 实收金额
     */
    private BigDecimal amount;

    /**
     * 备注
     */
    private String remark;

    /**
     * 订单取消原因
     */
    private String cancelReason;

    /**
     * 订单拒绝原因
     */

    private String rejectionReason;

    /**
     * 取消时间
     */

    private LocalDateTime cancelTime;

    /**
     * 创建时间
     */

    private LocalDateTime createTime;

    /**
     * 更新时间
     */

    private LocalDateTime updateTime;
}
