package pojo.vo.workspaces;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 商品销售DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GoodsSalesVO implements Serializable {
    /**
     * 商品名称
     */
    private String name;

    /**
     * 销量
     */
    private Long number;
}