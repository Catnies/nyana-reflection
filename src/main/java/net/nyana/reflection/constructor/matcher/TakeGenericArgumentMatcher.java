package net.nyana.reflection.constructor.matcher;

import net.nyana.reflection.type.matcher.TypeMatcher;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

final class TakeGenericArgumentMatcher implements ConstructorMatcher {
    private final TypeMatcher matcher;
    private final int index;

    TakeGenericArgumentMatcher(int index, TypeMatcher matcher) {
        this.matcher = matcher;
        this.index = index;
    }

    @Override
    public boolean matches(Constructor<?> constructor) {
        Type[] params = constructor.getGenericParameterTypes();
        if (this.index >= params.length) {
            return false;
        }
        return this.matcher.matches(params[this.index]);
    }
}
