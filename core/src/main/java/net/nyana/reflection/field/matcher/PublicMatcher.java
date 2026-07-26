package net.nyana.reflection.field.matcher;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

final class PublicMatcher implements FieldMatcher {
    public static final FieldMatcher INSTANCE = new PublicMatcher();

    private PublicMatcher() {}

    @Override
    public boolean matches(Field field) {
        return Modifier.isPublic(field.getModifiers());
    }
}
