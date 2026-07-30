package pojo.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmoployeePageDTO implements Serializable {
    private int page;
    private int pageSize;
    private String userName;
}
