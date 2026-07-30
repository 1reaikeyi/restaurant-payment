package start.exceptionHandle;

import common.constant.ErrorConstant;
import common.exception.BaseException;
import common.result.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BaseException.class)
    public Result exception(Exception e) {
        return Result.error(e.getMessage()+">>>>去联系管理员");
    }
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException e) {
        String message = e.getMessage();
        if (message.contains("Duplicate entry")) {
            String[] split = message.split("'");
            String username = split[1];
            String Message = username + ErrorConstant.USERNAME_EXIST;
            return Result.error(Message);
        } else {
            String Message = ErrorConstant.ERROR;
            return Result.error(Message+e.getMessage());
        }
    }


}