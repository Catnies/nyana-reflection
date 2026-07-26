package net.nyana.reflection.type.matcher;

import java.lang.reflect.Type;

// 原始类匹配器: 仅当目标类型就是指定的 Class 本身时匹配.
final class ClassMatcher implements TypeMatcher {
    private final Class<?> clazz; // 期望精确匹配的目标类

    ClassMatcher(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public boolean matches(Type type) {
        // 目标必须是 Class 且与期望类完全一致
        if (!(type instanceof Class<?> c)) {
            return false;
        }
        return this.clazz == c;
    }
}
