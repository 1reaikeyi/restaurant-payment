package pojo.dto.order;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单分页查询DTO
 */
@Data
public class OrdersPageDTO implements Serializable {

    /**
     * 页码
     */
    private Long page;

    /**
     * 每页记录数
     */
    private Long pageSize;


    /**
     * 电话
     */
   private String phone;

    /**
     * 用户id
     */
    private Long id;

}