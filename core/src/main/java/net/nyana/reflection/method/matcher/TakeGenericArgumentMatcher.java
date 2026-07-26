package net.nyana.reflection.method.matcher;

import net.nyana.reflection.type.matcher.TypeMatcher;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

final class TakeGenericArgumentMatcher implements MethodMatcher {
    private final TypeMatcher matcher;
    private final int index;

    TakeGenericArgumentMatcher(int index, TypeMatcher matcher) {
        this.matcher = matcher;
        this.index = index;
    }

    @Override
    public boolean matches(Method method) {
        Type[] params = method.getGenericParameterTypes();
        if (this.index >= params.length) {
            return false;
        }
        return this.matcher.matches(params[this.index]);
    }
}
