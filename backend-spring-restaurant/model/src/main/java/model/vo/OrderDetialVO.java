package model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetialVO implements Serializable {
    //订单id
    private Long id;
    //订单号
    private String orderNumber;

}
