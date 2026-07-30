package pojo.dto.category;

import lombok.Data;

import java.io.Serializable;

/**
 * 分类DTO
 */
@Data
public class CategoryDTO implements Serializable {

    /**
     * 主键
     */

    private Long id;

    /**
     * 分类分组标识，用于区分不同的分类组别
     */
    private Long type;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 排序
     */
    private Long sort;
    /**
     * 状态
     * 1正常 2停用
     */
    private Long status;

}