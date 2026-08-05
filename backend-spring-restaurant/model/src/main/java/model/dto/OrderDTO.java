package model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.entity.OrderDetail;
import model.entityenum.DeliveryStatusEnum;
import model.entityenum.OrderStatusEnum;
import model.entityenum.PayStatusEnum;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    /**
     * 主键
     */

    private Long id;

    /**
     * 订单状态：1待支付 2待商家接单 3商家接单制作中 4待骑手取餐 5骑手已取餐配送中 6骑手已送达 7订单已完成 8订单已取消
     */
    private OrderStatusEnum status;

    /**
     * 下单用户
     */
    private Long userId;

    /**
     * 用户名称
     */

    private String userName;

    /**
     * 收货人
     */

    private String consignee;

    /**
     * 手机号
     */

    private String phone;

    /**
     * 地址id
     */

    private Long addressId;

    /**
     * 地址
     */

    private String address;

    /**
     * 关联orderPay
     */

    private PayStatusEnum payStatus;

    /**
     * 备注
     */

    private String remark;

    /**
     * 配送方式：1立即送出 0选择具体时间
     */

    private DeliveryStatusEnum deliveryStatusEnum;

    /**
     * 配送时间
     */

    private LocalDateTime startDeliveryTime;

    /**
     * 预计送达时间
     */

    private LocalDateTime estimatedDeliveryTime;

    /**
     * 送达时间
     */

    private LocalDateTime deliveryTime;

    private List<OrderDetail> orderDetailList;


}
