package tech.qiwang.core;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
@Slf4j
public class RequireLeaderAdvice {

    /**
     * 在方法执行前增加一个是否是Leader的校验
     */
    @Around("@annotation(method)")
    public Object doInTransaction(ProceedingJoinPoint pjp, RequireLeader method) throws Throwable {
        Object result = null;
        if (log.isDebugEnabled()) {
            log.debug(pjp + "Leader 校验开始");
        }
        if (LeaderUtil.isLeader()) {
            result = pjp.proceed();
        }
        if (log.isDebugEnabled()) {
            log.debug(pjp + "Leader 校验结束");
        }
        return result;
    }
}
