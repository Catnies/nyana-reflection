package net.nyana.reflection.proxy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Type {

    Class<?> clazz() default Object.class;

    String[] name() default {};

    boolean ignoreRelocation() default false;
}
