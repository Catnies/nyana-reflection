package net.nyana.reflection.proxy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 将代理方法绑定为目标字段写入操作
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldSetter {

    /**
     * 可匹配的字段名, 会经过字段名 remap
     */
    String[] name();

    /**
     * 字段是否为 static
     */
    boolean isStatic() default false;

    /**
     * 交给 NyanaReflection active predicate 判断的版本或环境条件
     */
    String activeIf() default "";

    /**
     * 字段缺失时是否跳过该代理方法
     */
    boolean optional() default false;
}
