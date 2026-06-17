package net.nyana.reflection.constructor.matcher;

import net.nyana.reflection.type.matcher.TypeMatcher;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

final class TakeGenericArgumentsMatcher implements ConstructorMatcher {
    private final TypeMatcher[] arguments;

    TakeGenericArgumentsMatcher(TypeMatcher... arguments) {
        this.arguments = arguments;
    }

    @Override
    public boolean matches(Constructor<?> constructor) {
        Type[] params = constructor.getGenericParameterTypes();
        if (params.length != arguments.length) return false;
        for (int i = 0; i < this.arguments.length; i++) {
            if (!this.arguments[i].matches(params[i])) return false;
        }
        return true;
    }
}
