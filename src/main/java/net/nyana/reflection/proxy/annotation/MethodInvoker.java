package net.nyana.reflection.proxy.annotation;

import net.nyana.reflection.condition.annotation.Condition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 将代理方法绑定为目标方法调用操作
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodInvoker {
    /**
     * 启用此方法绑定前必须通过的全部条件
     */
    Condition[] conditions() default {};

    /**
     * 可匹配的方法名, 会经过方法名 remap
     */
    String[] name();

    /**
     * 目标方法是否为 static
     */
    boolean isStatic() default false;

    /**
     * 方法缺失时是否跳过该代理方法
     */
    boolean optional() default false;
}
