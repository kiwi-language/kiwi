//package org.metavm.common;
//
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.jetbrains.annotations.NotNull;
//import org.metavm.object.version.VersionManager;
//import org.metavm.util.ContextUtil;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//@Aspect
//@Component
//public class TypeFetchingAspect {
//
//    public static final Logger logger = LoggerFactory.getLogger(TypeFetchingAspect.class);
//
//    private final VersionManager versionManager;
//
//    public TypeFetchingAspect(VersionManager versionManager) {
//        this.versionManager = versionManager;
//    }
//
//    @Around("@annotation(org.springframework.web.bind.annotation.PostMapping)")
//    public Result<?> aroundPost(@NotNull ProceedingJoinPoint pjp) throws Throwable {
//        return process(pjp);
//    }
//
//    @Around("@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
//    public Result<?> aroundDelete(@NotNull ProceedingJoinPoint pjp) throws Throwable {
//        return process(pjp);
//    }
//
//    @Around("@annotation(org.springframework.web.bind.annotation.GetMapping)")
//    public Result<?> aroundGet(@NotNull ProceedingJoinPoint pjp) throws Throwable {
//        return process(pjp);
//    }
//
//    private Result<?> process(@NotNull ProceedingJoinPoint pjp) throws Throwable {
//        Result<?> result = (Result<?>) pjp.proceed();
//        var metaVersion = ContextUtil.getMetaVersion();
//        if(metaVersion != null && metaVersion != -1L && ContextUtil.isLoggedIn())
//            result.setMetaPatch(versionManager.pull(metaVersion));
//        return result;
//    }
//
//}
