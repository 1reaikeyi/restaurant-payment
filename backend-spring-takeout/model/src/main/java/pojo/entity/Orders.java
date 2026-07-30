package pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pojo.entityenum.DeliveryStatusEnum;
import pojo.entityenum.OrderStatusEnum;
import pojo.entityenum.PayStatusEnum;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("orders")
public class Orders implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单状态：1待付款 2待接单 3已接单 4派送中 5已完成 6已取消 7退款
     */
    @EnumValue
    @TableField("status")
    private OrderStatusEnum status;

    /**
     * 下单用户id
     */
    @TableField(value = "user_id",fill = FieldFill.INSERT)
    private Long userId;

    /**
     * 地址id
     */
    @TableField("address_book_id")
    private Long addressBookId;
    /**
     * 用户名
     */
    @TableField("username")
    private String userName;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 地址
     */
    @TableField("address")
    private String address;

    /**
     * 收货人
     */
    @TableField("consignee")
    private String consignee;

    /**
     * 下单时间
     */
    @TableField("order_time")
    private LocalDateTime orderTime;

    /**
     * 支付方式：1支付宝，2微信
     */
    @TableField("pay_method")
    private Long payMethod;

    /**
     * 支付状态：0未支付 1已支付 2退款
     */
    @EnumValue
    @TableField("pay_status")
    private PayStatusEnum payStatus;

    /**
     * 实收金额
     */
    @TableField("amount")
    private BigDecimal amount;
    /**
     * 结账时间
     */
    @TableField("checkout_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkoutTime;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;



    /**
     * 订单取消原因
     */
    @TableField("cancel_reason")
    private String cancelReason;

    /**
     * 订单拒绝原因
     */
    @TableField("rejection_reason")
    private String rejectionReason;

    /**
     * 订单取消时间
     */
    @TableField("cancel_time")
    private LocalDateTime cancelTime;

    /**
     * 配送状态：1立即送出 0选择具体时间
     */
    @EnumValue
    @TableField("delivery_status")
    private DeliveryStatusEnum deliveryStatusEnum;
    /**
     * 配送开始时间
     */
    @TableField("start_delivery_time")
    private LocalDateTime startDeliveryTime;
    /**
     * 预计送达时间
     */
    @TableField("estimated_delivery_time")
    private LocalDateTime estimatedDeliveryTime;

    /**
     * 送达时间
     */
    @TableField("delivery_time")
    private LocalDateTime deliveryTime;

}