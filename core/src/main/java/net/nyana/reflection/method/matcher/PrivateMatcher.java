package net.nyana.reflection.method.matcher;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

final class PrivateMatcher implements MethodMatcher {
    public static final MethodMatcher INSTANCE = new PrivateMatcher();

    private PrivateMatcher() {}

    @Override
    public boolean matches(Method method) {
        return Modifier.isPrivate(method.getModifiers());
    }
}
