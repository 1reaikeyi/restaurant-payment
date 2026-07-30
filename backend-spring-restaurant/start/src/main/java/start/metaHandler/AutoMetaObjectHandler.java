package start.metaHandler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import common.constant.AutoFillConstant;
import common.constant.JwtClaimsConstant;
import common.localContextHolder.ThreadLocalContextHolder;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class AutoMetaObjectHandler implements MetaObjectHandler {
    private Long getEmpId() {
        Map<String, Object> claims = ThreadLocalContextHolder.get();
        if (claims == null) {
            return 0L;
        }
        Object currentEmpId = claims.get(JwtClaimsConstant.EMP_ID);
        if (currentEmpId == null) {
            return 0L;
        }
        return Long.parseLong(currentEmpId.toString());
    }
    private Long getUserId() {
        Map<String, Object> claims = ThreadLocalContextHolder.get();
        if (claims == null) {
            return 0L;
        }
        Object currentUserId = claims.get(JwtClaimsConstant.USER_ID);
        if (currentUserId == null) {
            return 0L;
        }
        return Long.parseLong(currentUserId.toString());
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        this.setFieldValByName(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.now(), metaObject);
        this.setFieldValByName(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.now(), metaObject);

        Long empId = getEmpId();
        Long userId = getUserId();
        this.setFieldValByName(AutoFillConstant.SET_CREATE_USER, empId, metaObject);
        this.setFieldValByName(AutoFillConstant.SET_UPDATE_USER, empId, metaObject);
        this.setFieldValByName(AutoFillConstant.SET_CREATE_USER, userId, metaObject);
        this.setFieldValByName(AutoFillConstant.SET_UPDATE_USER, userId, metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.now(), metaObject);

        Long empId = getEmpId();
        Long userId = getUserId();
        this.setFieldValByName(AutoFillConstant.SET_UPDATE_USER, empId, metaObject);
        this.setFieldValByName(AutoFillConstant.SET_UPDATE_USER, userId, metaObject);
    }
}