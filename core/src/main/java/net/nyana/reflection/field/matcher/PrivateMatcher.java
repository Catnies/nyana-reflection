package net.nyana.reflection.field.matcher;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

final class PrivateMatcher implements FieldMatcher {
    public static final FieldMatcher INSTANCE = new PrivateMatcher();

    private PrivateMatcher() {}

    @Override
    public boolean matches(Field field) {
        return Modifier.isPrivate(field.getModifiers());
    }
}
