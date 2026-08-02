package model.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantCategoryDTO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 分类分组标识，用于区分不同的分类组别
     * 例如：1,2,3,4
     */
    private Long type;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 顺序优先级
     */
    private Long sort;

    /**
     * 分类状态：0禁用 1启用
     */
    private Long status;

}
