package net.nyana.reflection.method.matcher;

import java.lang.reflect.Method;

final class ReturnTypeMatcher implements MethodMatcher {
    private final Class<?> returnType;

    ReturnTypeMatcher(Class<?> returnType) {
        this.returnType = returnType;
    }

    @Override
    public boolean matches(Method method) {
        return method.getReturnType() == this.returnType;
    }
}
