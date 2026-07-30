package pojo.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单统计DTO
 * 包含所有订单状态的统计数量
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatisticsDTO implements Serializable {

    // 待支付数量 (PENDING_PAYMENT)
    private Long pendingPayment;

    // 待商家接单数量 (PENDING_MERCHANT_ACCEPT)
    private Long toBeConfirmed;

    // 商家制作中数量 (MERCHANT_COOKING)
    private Long merchantCooking;

    // 待骑手取餐数量 (PENDING_RIDER_PICK)
    private Long pendingRiderPick;

    // 骑手配送中数量 (RIDER_DELIVERING)
    private Long riderDelivering;

    // 骑手已送达数量 (RIDER_ARRIVED)
    private Long riderArrived;

    // 订单已完成数量 (COMPLETED)
    private Long completed;

    // 订单已取消数量 (CANCELLED)
    private Long cancelled;
}
