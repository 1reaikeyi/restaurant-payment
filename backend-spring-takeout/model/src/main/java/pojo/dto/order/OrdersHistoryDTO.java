package pojo.dto.order;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class OrdersHistoryDTO implements  java.io.Serializable{

    /**
     * 页码
     */
    private Long page;

    /**
     * 每页记录数
     */
    private Long pageSize;
    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 截至时间
     */
    private String endTime;
}
