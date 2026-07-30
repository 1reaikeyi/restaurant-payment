package pojo.dto.setmeal;

import lombok.Data;

import java.io.Serializable;

/**
 * 套餐分页查询DTO
 */
@Data
public class SetmealPageDTO implements Serializable {

    /**
     * 页码
     */
    private Long page;

    /**
     * 每页记录数
     */
    private Long pageSize;

    /**
     * 套餐名称
     */
    private String name;

    /**
     * 分类id
     */
    private Long categoryId;

    /**
     * 状态：0禁用 1启用
     */
    private Long status;

}