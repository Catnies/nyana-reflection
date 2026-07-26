package net.nyana.reflection.condition.annotation;


import net.nyana.reflection.condition.CustomCondition;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明一个代理绑定前必须通过的条件
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface Condition {

    Class<? extends CustomCondition> type();

    String[] value() default "";
}