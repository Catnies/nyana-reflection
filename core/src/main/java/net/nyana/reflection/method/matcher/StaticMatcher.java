package net.nyana.reflection.method.matcher;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

final class StaticMatcher implements MethodMatcher {
    public static final MethodMatcher INSTANCE = new StaticMatcher();

    private StaticMatcher() {}

    @Override
    public boolean matches(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }
}
