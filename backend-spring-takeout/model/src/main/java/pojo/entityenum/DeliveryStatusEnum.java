package pojo.entityenum;

import com.baomidou.mybatisplus.annotation.EnumValue;

public enum DeliveryStatusEnum {
    /**
     * 配送状态：1立即送出 0选择具体时间
     */
    NOW(1L, "立即送出"),
    BOOK_TIME(0L, "选择时间");
    @EnumValue
    private Long value;
    private String name;
    private DeliveryStatusEnum(Long value, String name) {
        this.name = name;
        this.value = value;
    }
}
