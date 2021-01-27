package com.wte.apiuser.component;

import cn.hutool.core.map.MapUtil;
import com.wt.wte.wtebase.common.api.ApiException;
import com.wt.wte.wtebase.common.api.ExceptionConstants;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 接口异常处理切面
 * Create by chenglong on 2021/1/22
 */
@Aspect
@Component
@Order(3)
@Slf4j
public class ExceptionAspect {
    @Around("execution(public * com.wte.apiuser.service.impl.*.*(..))")
    public Object throwException(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = null;
        //获得类名
        String clazzName = joinPoint.getTarget().getClass().getSimpleName();
        //获得方法名
        String methodName = joinPoint.getSignature().getName();
        try{
            result = joinPoint.proceed();
        } catch (ApiException apiException){
            throw apiException;
        } catch (Exception e){
            log.info(clazzName+"."+methodName+"发生异常，异常信息：{}",e.getMessage());
            throw new ApiException(ExceptionConstants.API_EXCEPTION, MapUtil.of("msg", e.getMessage()));
        }
        return result;
    }
}
