package net.nyana.reflection.method.matcher;

import java.lang.reflect.Method;

final class TakeArgumentMatcher implements MethodMatcher {
    private final Class<?> argument;
    private final int index;

    TakeArgumentMatcher(int index, Class<?> argument) {
        this.argument = argument;
        this.index = index;
    }

    @Override
    public boolean matches(Method method) {
        Class<?>[] params = method.getParameterTypes();
        if (this.index >= params.length) {
            return false;
        }
        return this.argument == params[this.index];
    }
}
