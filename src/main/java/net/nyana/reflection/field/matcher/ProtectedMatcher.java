package net.nyana.reflection.field.matcher;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

final class ProtectedMatcher implements FieldMatcher {
    public static final FieldMatcher INSTANCE = new ProtectedMatcher();

    private ProtectedMatcher() {}

    @Override
    public boolean matches(Field field) {
        return Modifier.isProtected(field.getModifiers());
    }
}
