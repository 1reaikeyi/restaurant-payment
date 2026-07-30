package start.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import start.annotation.Info;

import java.lang.reflect.Method;
@Slf4j
@Aspect // 标记为AOP切面类
@Component
public class ServiceInterceptAspect {

    @Around("@annotation(start.annotation.Info)")
    public Object interceptServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取注解信息和目标方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = signature.getMethod(); // 目标方法
        Info annotation = targetMethod.getAnnotation(Info.class); // 获取自定义注解
        String methodDesc = annotation.desc(); // 注解的描述属性
        String className = joinPoint.getTarget().getClass().getName(); // 目标类名（比如com.rent.service.RentService）
        String methodName = targetMethod.getName(); // 目标方法名（比如queryRentInfo）
        Object[] methodArgs = joinPoint.getArgs(); // 方法入参
        long startTime = System.currentTimeMillis();
        Object result = null;
        log.info("=>执行：{}", methodDesc);
        try {
            // 3. 执行目标方法（核心业务逻辑）
            result = joinPoint.proceed();
            long costTime = System.currentTimeMillis() - startTime;
            log.info("=>目标类：{}, 目标方法：{}", className, methodName);
            log.info("耗时：{}ms | Rerurn：{}", costTime, result);
        } catch (Exception e) {
            // 5. 方法执行异常：打印异常信息
            long costTime = System.currentTimeMillis() - startTime;
            log.error("=>目标类：{}, 目标方法：{}", className, methodName);
            log.error("耗时：{}ms | 异常信息：{}", costTime, e.getMessage());
            throw e;
        }
        return result;
    }
}
