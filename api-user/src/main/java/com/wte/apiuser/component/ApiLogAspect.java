package com.wte.apiuser.component;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import com.wt.wte.wtebase.common.api.ApiLog;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * 统一日志处理切面
 * Create by chenglong on 2021/1/15
 */
@Aspect
@Component
@Order(2)
@Slf4j
public class ApiLogAspect {

    @Pointcut("execution(public * com.wte.apiuser.controller.*.*(..))")
    public void apiLog(){
    }

    @Before(value = "apiLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable{
    }

    @AfterReturning(value = "apiLog()", returning = "ret")
    public void doAfterReturning(Object ret) throws Throwable{
    }

    @Around(value = "apiLog()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable{
        //获取当前请求对象
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        String urlStr = request.getRequestURL().toString();
        Date startTime = DateUtil.date();//记录开始时间
        Object result = pjp.proceed();
        Date endTime = DateUtil.date();
        ApiLog apiLog = new ApiLog();
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        Method method = methodSignature.getMethod();
        if(method.isAnnotationPresent(ApiOperation.class)){
            ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
            apiLog.setDescription(apiOperation.value());
        }
//        apiLog.setUsername();
        apiLog.setStartTime(DateUtil.formatDateTime(startTime));
        apiLog.setBasePath(StrUtil.removeSuffix(urlStr, URLUtil.url(urlStr).getPath()));
        apiLog.setIp(request.getRemoteUser());
        apiLog.setUri(request.getRequestURI());
        apiLog.setUrl(urlStr);
        apiLog.setSpendTime((int) (endTime.getTime()-startTime.getTime())+"ms");
        apiLog.setResult(result);
        apiLog.setMethod(request.getMethod());
        apiLog.setParameter(getParameter(method,pjp.getArgs()));
        log.info("\r\n=================API接口参数======================\r\n{}", JSONUtil.toJsonPrettyStr(JSONUtil.parse(apiLog)));
        return result;
    }

    /**
     * 根据方法和传入的参数获取请求参数
     * @param method
     * @param args
     * @return
     */
    private Object getParameter(Method method, Object[] args) {
        List<Object> argList = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        for(int i=0;i<parameters.length;i++){
            RequestBody requestBody = parameters[i].getAnnotation(RequestBody.class);
            if(requestBody!=null){
                argList.add(args[i]);
            }
            RequestParam requestParam = parameters[i].getAnnotation(RequestParam.class);
            if(requestParam!=null){
                Map<String,Object> map = new HashMap<>();
                String key = parameters[i].getName();
                if(StrUtil.isNotEmpty(requestParam.value())){
                    key = requestParam.value();
                }
                map.put(key, args[i]);
                argList.add(map);
            }
        }
        if(argList.size()==0){
            return null;
        }else if(argList.size()==1){
            return argList.get(0);
        }else {
            return argList;
        }
    }

}
