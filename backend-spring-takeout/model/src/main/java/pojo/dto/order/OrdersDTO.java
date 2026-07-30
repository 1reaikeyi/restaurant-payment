package pojo.dto.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import pojo.entity.OrderDetail;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单DTO
 */
@Data
public class OrdersDTO implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 订单状态：1待付款，2待派送，3已派送，4已完成，5已取消
     */
    private Long status;

    /**
     * 下单用户id
     */
    private Long userId;

    /**
     * 地址id
     */
    private Long addressBookId;

    /**
     * 下单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderTime;

    /**
     * 结账时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkoutTime;

    /**
     * 支付方式：2微信，1支付宝
     */
    private Long payMethod;

    /**
     * 实收金额
     */
    private BigDecimal amount;

    /**
     * 备注
     */
    private String remark;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 地址
     */
    private String address;

    /**
     * 收货人
     */
    private String consignee;

    /**
     * 订单详情
     */
    private List<OrderDetail> orderDetails;

}