package net.nyana.reflection.field.matcher;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

final class InstanceMatcher implements FieldMatcher {
    public static final FieldMatcher INSTANCE = new InstanceMatcher();

    private InstanceMatcher() {}

    @Override
    public boolean matches(Field field) {
        return !Modifier.isStatic(field.getModifiers());
    }
}
