package net.nyana.reflection.constructor.matcher;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

final class PublicMatcher implements ConstructorMatcher {
    public static final ConstructorMatcher INSTANCE = new PublicMatcher();

    private PublicMatcher() {}

    @Override
    public boolean matches(Constructor<?> constructor) {
        return Modifier.isPublic(constructor.getModifiers());
    }
}
