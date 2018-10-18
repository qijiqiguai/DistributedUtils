package tech.qiwang.core;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
public class RequireLeaderAdvice {

    @Autowired
    LeaderI leaderI;

    /**
     * 在方法执行前增加一个是否是Leader的校验
     */
    @Around("@annotation(method)")
    public Object doInTransaction(ProceedingJoinPoint pjp, RequireLeader method) throws Throwable {
        Object result = null;
        log.debug(pjp + "Leader 校验开始");
        if (leaderI.isLeader()) {
            result = pjp.proceed();
        }else {
            log.debug("Not Leader");
        }
        log.debug(pjp + "Leader 校验结束");
        return result;
    }
}
