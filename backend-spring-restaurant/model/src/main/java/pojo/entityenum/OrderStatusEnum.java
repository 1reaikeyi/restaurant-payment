package pojo.entityenum;


import com.baomidou.mybatisplus.annotation.EnumValue;

public enum OrderStatusEnum {
    /**
     * 订单状态：
     * 1 待支付：下单未付款
     * 2 待商家接单：已付款，商家还没接单
     * 3 商家接单，制作中：商家确认接单，正在做菜
     * 4 待骑手取餐：商家出餐完成，骑手还没到店
     * 5 骑手已取餐，配送中：骑手拿到餐，在路上，实时看定位
     * 6 骑手已送达：骑手点送达，等待用户确认
     * 7 订单已完成：系统自动确认收货
     * 8 订单已取消：未接单退款、商家拒单、超时取消、售后全额退款
     */
    PENDING_PAYMENT(1L, "待支付：下单未付款"),
    PENDING_MERCHANT_ACCEPT(2L, "待商家接单：已付款，商家还没接单"),
    MERCHANT_COOKING(3L, "商家接单,制作中：商家确认接单，正在做菜"),
    PENDING_RIDER_PICK(4L, "待骑手取餐：商家出餐完成，骑手还没到店"),
    RIDER_DELIVERING(5L, "骑手已取餐，配送中：骑手拿到餐，在路上，实时看定位"),
    RIDER_ARRIVED(6L, "骑手已送达：骑手点送达，等待用户确认"),
    COMPLETED(7L, "订单已完成：系统自动确认收货"),
    CANCELLED(8L, "订单已取消：未接单退款、商家拒单、超时取消、售后全额退款");
    @EnumValue
    private final Long code;
    private final String text;

    OrderStatusEnum(Long code, String fullText) {
        this.code = code;
        this.text = fullText;
    }

    // getter
    public Long getCode() {
        return code;
    }
    public String getFullText() {
        return text;
    }

}
