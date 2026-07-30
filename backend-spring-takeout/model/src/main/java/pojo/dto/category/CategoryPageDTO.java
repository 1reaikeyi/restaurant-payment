package pojo.dto.category;

import lombok.Data;

import java.io.Serializable;

/**
 * 分类分页查询DTO
 */
@Data
public class CategoryPageDTO implements Serializable {

    /**
     * 页码
     */
    private Long page;

    /**
     * 每页记录数
     */
    private Long pageSize;

    /**
     * 分类分组标识，根据type筛选
     */
    private Long type;

    /**
     * 分类名称，用于模糊查询
     */
    private String name;

}