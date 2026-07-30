package pojo.vo.workspaces;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单概览数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderOverViewVO implements Serializable {
    //已退款订单
    private Long refundOrders;

    //已完成数量
    private Long completedOrders;

    //全部订单
    private Long allOrders;

}
