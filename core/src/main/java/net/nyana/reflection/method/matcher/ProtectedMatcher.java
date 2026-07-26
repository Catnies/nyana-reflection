package net.nyana.reflection.method.matcher;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

final class ProtectedMatcher implements MethodMatcher {
    public static final MethodMatcher INSTANCE = new ProtectedMatcher();

    private ProtectedMatcher() {}

    @Override
    public boolean matches(Method method) {
        return Modifier.isProtected(method.getModifiers());
    }
}
