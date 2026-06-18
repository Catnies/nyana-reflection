package net.nyana.reflection.proxy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明一个接口是反射代理接口, 并指定它要代理的真实目标类
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReflectionProxy {

    /**
     * 直接指定目标类, 优先级高于 name
     */
    Class<?> clazz() default Object.class;

    /**
     * 通过一个或多个类名查找目标类, 会经过 NyanaReflection 的 Remapper
     */
    String[] name() default {};

    /**
     * 是否忽略 relocation, 会把类名中的 "{}" 替换为 "."
     */
    boolean ignoreRelocation() default false;

    /**
     * 目标类缺失时是否允许返回 null
     */
    boolean nullable() default false;

    /**
     * 交给 NyanaReflection active predicate 判断的版本或环境条件
     */
    String activeIf() default "";
}
