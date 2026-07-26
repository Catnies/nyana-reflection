package net.nyana.reflection.constructor.matcher;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

final class PrivateMatcher implements ConstructorMatcher {
    public static final ConstructorMatcher INSTANCE = new PrivateMatcher();

    private PrivateMatcher() {}

    @Override
    public boolean matches(Constructor<?> constructor) {
        return Modifier.isPrivate(constructor.getModifiers());
    }
}
