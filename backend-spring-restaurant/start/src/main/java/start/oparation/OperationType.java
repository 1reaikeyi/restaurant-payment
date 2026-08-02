package start.oparation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import start.security.SecurityContextParam;

/**
 * 数据库操作类型OperationType
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class OperationType {
    private String operation;
    private Long id;
    private String status;
    private Object message;

    public static OperationType ok(String operation,Object message) {
        OperationType operationType = new OperationType();
        operationType.operation = operation;
        operationType.id = SecurityContextParam.getCurrentUserId();
        operationType.status = "SUCCESS";
        operationType.message = message;
        // 改为 info 级别，确保默认日志配置下可见（debug 默认不输出）
        log.info("用户ID:"+operationType.id+", 执行操作:"+operationType.operation+", "+message+", "+operationType.status);
        return operationType;
    }

    public static OperationType error(String operation,Object message) {
        OperationType operationType = new OperationType();
        operationType.operation = operation;
        operationType.id = SecurityContextParam.getCurrentUserId();
        operationType.status = "ERROR";
        operationType.message = message;
        // 改为 info 级别，确保异常操作日志在默认配置下可见
        log.info("用户ID:"+operationType.id+", 执行操作:"+operationType.operation+", "+message+", "+operationType.status);
        return operationType;
    }
}
