package net.nyana.reflection.method.matcher;

import net.nyana.reflection.type.matcher.TypeMatcher;

import java.lang.reflect.Method;

final class GenericReturnTypeMatcher implements MethodMatcher {
    private final TypeMatcher matcher;

    GenericReturnTypeMatcher(TypeMatcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public boolean matches(Method method) {
        return this.matcher.matches(method.getGenericReturnType());
    }
}
