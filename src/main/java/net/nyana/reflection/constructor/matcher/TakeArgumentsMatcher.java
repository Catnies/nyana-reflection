package net.nyana.reflection.constructor.matcher;

import java.lang.reflect.Constructor;

final class TakeArgumentsMatcher implements ConstructorMatcher {
    private final Class<?>[] arguments;

    TakeArgumentsMatcher(Class<?>... arguments) {
        this.arguments = arguments;
    }

    @Override
    public boolean matches(Constructor<?> constructor) {
        Class<?>[] params = constructor.getParameterTypes();
        if (params.length != arguments.length) return false;
        for (int i = 0; i < this.arguments.length; i++) {
            if (this.arguments[i] != params[i])
                return false;
        }
        return true;
    }
}
