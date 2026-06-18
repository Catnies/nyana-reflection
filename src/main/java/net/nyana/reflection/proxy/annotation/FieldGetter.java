package net.nyana.reflection.proxy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldGetter {

    String[] name();

    boolean isStatic() default false;

    String activeIf() default "";

    boolean optional() default false;
}
