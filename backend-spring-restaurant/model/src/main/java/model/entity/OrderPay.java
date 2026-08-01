package model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.entityenum.PayStatusEnum;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单支付实体类（对应 order_pay 表）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("order_pay")
public class OrderPay implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联orderId
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 下单时间
     */
    @TableField("order_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderTime;

    /**
     * 支付方式 1微信,2支付宝
     */
    @TableField("pay_method")
    private Long payMethod;

    /**
     * 支付状态 0未支付 1已支付 2退款
     */
    @EnumValue
    @TableField("pay_status")
    private PayStatusEnum payStatus;

    /**
     * 结账时间
     */
    @TableField("checkout_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkoutTime;

    /**
     * 实收金额
     */
    @TableField("amount")
    private BigDecimal amount;

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
     * 取消时间
     */
    @TableField("cancel_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @TableField(value = "create_user", fill = FieldFill.INSERT)
    private Long createUser;

    /**
     * 修改人
     */
    @TableField(value = "update_user", fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

}
