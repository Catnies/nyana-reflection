package net.nyana.reflection.field.matcher;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

final class StaticMatcher implements FieldMatcher {
    public static final FieldMatcher INSTANCE = new StaticMatcher();

    private StaticMatcher() {}

    @Override
    public boolean matches(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }
}
