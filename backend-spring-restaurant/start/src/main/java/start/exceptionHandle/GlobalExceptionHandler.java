package start.exceptionHandle;

import common.constant.ErrorConstant;
import common.exception.BaseException;
import common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
@Slf4j  // 添加日志，方便排查未知异常
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常 BaseException
     * 返回 200 状态码 + Result.error（业务错误由前端提示文案决定）
     */
    @ExceptionHandler(BaseException.class)
    public Result exception(BaseException e) {
        return Result.error(e.getMessage() + ">>>>去联系管理员");
    }

    /**
     * 处理数据库唯一约束冲突（如重复用户名）
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException e) {
        String message = e.getMessage();
        if (message.contains("Duplicate entry")) {
            String[] split = message.split("'");
            String username = split[1];
            String Message = username + ErrorConstant.USERNAME_EXIST;
            return Result.error(Message);
        } else {
            return Result.error(ErrorConstant.ERROR + e.getMessage());
        }
    }


}