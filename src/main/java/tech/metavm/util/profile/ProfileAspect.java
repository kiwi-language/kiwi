package tech.metavm.util.profile;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import tech.metavm.util.ContextUtil;

@Aspect
@Component
public class ProfileAspect {

    @Around("@annotation(tech.metavm.util.profile.Traced)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        var name = String.format("%s.%s",
                pjp.getSignature().getDeclaringType().getSimpleName(), pjp.getSignature().getName());
        try(var ignored = ContextUtil.getProfiler().enter(name)) {
            return pjp.proceed();
        }
    }

}
