package net.nyana.reflection.constructor.matcher;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

final class ProtectedMatcher implements ConstructorMatcher {
    public static final ConstructorMatcher INSTANCE = new ProtectedMatcher();

    private ProtectedMatcher() {}

    @Override
    public boolean matches(Constructor<?> constructor) {
        return Modifier.isProtected(constructor.getModifiers());
    }
}
