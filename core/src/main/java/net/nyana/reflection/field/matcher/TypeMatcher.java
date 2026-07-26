package net.nyana.reflection.field.matcher;

import java.lang.reflect.Field;

// 字段类型匹配器: 字段的声明类型与目标类型完全一致时匹配(不含泛型信息).
final class TypeMatcher implements FieldMatcher {
    private final Class<?> type; // 期望的字段类型

    TypeMatcher(Class<?> type) {
        this.type = type;
    }

    @Override
    public boolean matches(Field field) {
        return this.type.equals(field.getType());
    }
}
