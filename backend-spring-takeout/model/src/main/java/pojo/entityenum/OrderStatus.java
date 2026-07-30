package pojo.entityenum;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Data;


public enum OrderStatus {
    /**
     * 订单状态：1待付款 2已付款，寻找商家 3制作中 4正在派送 5已送达 6取消支付 7退款
     */
    PENDING(1L, "待付款"),
    ACCEPTED(2L, "已支付,寻找商家"),
    COMFIRM(3L, "制作中"),
    DELIVERING(4L, "正在派送"),
    COMPLETED(5L, "已送达"),
    CANCELLED(6L, "取消支付"),
    REFUNDED(7L, "退款");
    @EnumValue
    private Long status;
    private String name;
    private OrderStatus(Long status, String name) {
        this.status = status;
        this.name = name;
    }

}
