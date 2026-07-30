package pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
public class CategoryVO implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 分类类型，用于区分不同的分类组别
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
     * 状态：0停售 1起售
     */
    private Long status;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}
