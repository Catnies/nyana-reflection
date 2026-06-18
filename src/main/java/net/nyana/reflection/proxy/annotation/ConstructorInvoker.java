package net.nyana.reflection.proxy.annotation;

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
     * 交给 NyanaReflection active predicate 判断的版本或环境条件
     */
    String activeIf() default "";

    /**
     * 构造器缺失时是否跳过该代理方法
     */
    boolean optional() default false;
}
