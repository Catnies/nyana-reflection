package net.nyana.reflection.method.matcher;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

final class FinalMatcher implements MethodMatcher {
    public static final MethodMatcher INSTANCE = new FinalMatcher();

    private FinalMatcher() {}

    @Override
    public boolean matches(Method method) {
        return Modifier.isFinal(method.getModifiers());
    }
}
