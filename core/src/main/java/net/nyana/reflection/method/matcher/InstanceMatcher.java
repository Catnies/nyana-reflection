package net.nyana.reflection.method.matcher;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

final class InstanceMatcher implements MethodMatcher {
    public static final MethodMatcher INSTANCE = new InstanceMatcher();

    private InstanceMatcher() {}

    @Override
    public boolean matches(Method method) {
        return !Modifier.isStatic(method.getModifiers());
    }
}
