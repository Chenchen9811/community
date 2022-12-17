package com.chen.community.Aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

//@Component
//@Aspect
public class AlphaAspect {

    // 第一个*:方法的返回值，代表什么返回值都行
    // 第二个*：该包下的所有组件
    // 第三个*：组件下的所有方法
    // (..)：方法的所有参数
    // 代表所有都要处理
    @Pointcut("execution(* com.chen.community.service.*.*(..))")
    public void pointcut() {

    }

    @Before("pointcut()")
    public void before() {
        System.out.println("before method");
    }

    @After("pointcut()")
    public void after() {
        System.out.println("after method");
    }

    @AfterReturning("pointcut()")
    public void afterReturning() {
        System.out.println("afterReturning");
    }

    @AfterThrowing("pointcut()")
    public void afterThrowing() {
        System.out.println("afterThrowing");
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("around before target method");
        Object obj = joinPoint.proceed();// 调用目标组件的方法
        System.out.println("around after target method");
        return obj;
    }
}
