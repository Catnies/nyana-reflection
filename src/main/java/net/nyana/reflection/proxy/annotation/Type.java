package net.nyana.reflection.proxy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 覆盖代理方法参数对应的真实目标参数类型
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Type {

    /**
     * 通过代理接口类型间接解析真实目标类型
     */
    Class<?> clazz() default Object.class;

    /**
     * 通过一个或多个类名查找目标参数类型, 会经过 NyanaReflection 的 Remapper
     */
    String[] name() default {};

    /**
     * 是否屏蔽 relocation, 会将类名中的 "{}" 替换为 "."
     */
    boolean ignoreRelocation() default false;
}
