package net.nyana.reflection.method.matcher;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

final class PublicMatcher implements MethodMatcher {
    public static final MethodMatcher INSTANCE = new PublicMatcher();

    private PublicMatcher() {}

    @Override
    public boolean matches(Method method) {
        return Modifier.isPublic(method.getModifiers());
    }
}
