package net.nyana.reflection.field.matcher;

import net.nyana.reflection.NyanaReflection;

import java.lang.reflect.Field;

final class NameMatcher implements FieldMatcher {
    private final String name;
    private final boolean remap;

    NameMatcher(String name, boolean remap) {
        this.name = name;
        this.remap = remap;
    }

    @Override
    public boolean matches(Field field) {
        if (!this.remap || NyanaReflection.getRemapper().isNoOp()) {
            return field.getName().equals(this.name);
        } else {
            return field.getName().equals(NyanaReflection.getRemapper().remapFieldName(field.getDeclaringClass(), this.name));
        }
    }
}
