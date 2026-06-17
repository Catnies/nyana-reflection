package net.nyana.reflection.field.matcher;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

final class FinalMatcher implements FieldMatcher {
    public static final FieldMatcher INSTANCE = new FinalMatcher();

    private FinalMatcher() {}

    @Override
    public boolean matches(Field field) {
        return Modifier.isFinal(field.getModifiers());
    }
}
