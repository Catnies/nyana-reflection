package net.nyana.reflection.constructor.matcher;

import java.lang.reflect.Constructor;

final class TakeArgumentMatcher implements ConstructorMatcher {
    private final Class<?> argument;
    private final int index;

    TakeArgumentMatcher(int index, Class<?> argument) {
        this.argument = argument;
        this.index = index;
    }

    @Override
    public boolean matches(Constructor<?> constructor) {
        Class<?>[] params = constructor.getParameterTypes();
        if (this.index >= params.length) {
            return false;
        }
        return this.argument == params[this.index];
    }
}
