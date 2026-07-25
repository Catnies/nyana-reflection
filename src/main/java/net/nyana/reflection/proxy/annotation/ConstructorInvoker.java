package net.nyana.reflection.proxy.annotation;

import net.nyana.reflection.condition.annotation.Condition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 将代理方法绑定为目标构造器调用操作
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConstructorInvoker {
    /**
     * 启用此构造器绑定前必须通过的全部条件
     */
    Condition[] conditions() default {};

    /**
     * 构造器缺失时是否跳过该代理方法
     */
    boolean optional() default false;
}
