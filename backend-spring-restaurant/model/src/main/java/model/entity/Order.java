package model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.entityenum.DeliveryStatusEnum;
import model.entityenum.OrderStatusEnum;
import model.entityenum.PayStatusEnum;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单实体类（对应 order 表）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("order")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单状态：1待支付 2待商家接单 3商家接单制作中 4待骑手取餐 5骑手已取餐配送中 6骑手已送达 7订单已完成 8订单已取消
     */
    @EnumValue
    @TableField("status")
    private OrderStatusEnum status;

    /**
     * 下单用户
     */
    @TableField(value = "user_id", fill = FieldFill.INSERT)
    private Long userId;

    /**
     * 用户名称
     */
    @TableField("username")
    private String userName;

    /**
     * 收货人
     */
    @TableField("consignee")
    private String consignee;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 地址id
     */
    @TableField("address_id")
    private Long addressId;

    /**
     * 地址
     */
    @TableField("address")
    private String address;

    /**
     * 关联orderPay
     */
    @EnumValue
    @TableField("pay_status")
    private PayStatusEnum payStatus;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 配送方式：1立即送出 0选择具体时间
     */
    @EnumValue
    @TableField("delivery_status")
    private DeliveryStatusEnum deliveryStatusEnum;

    /**
     * 配送时间
     */
    @TableField("start_delivery_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDeliveryTime;

    /**
     * 预计送达时间
     */
    @TableField("estimated_delivery_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedDeliveryTime;

    /**
     * 送达时间
     */
    @TableField("delivery_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveryTime;

}
