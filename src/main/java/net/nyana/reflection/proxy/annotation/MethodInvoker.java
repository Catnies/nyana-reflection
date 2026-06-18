package net.nyana.reflection.proxy.annotation;

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
     * 可匹配的方法名, 会经过方法名 remap
     */
    String[] name();

    /**
     * 目标方法是否为 static
     */
    boolean isStatic() default false;

    /**
     * 交给 NyanaReflection active predicate 判断的版本或环境条件
     */
    String activeIf() default "";

    /**
     * 方法缺失时是否跳过该代理方法
     */
    boolean optional() default false;
}
