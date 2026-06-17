package net.nyana.reflection.field.matcher;

import java.lang.reflect.Field;

// 字段泛型类型匹配器: 把字段的泛型类型(含参数化信息)委托给通用的类型匹配器判定.
final class GenericTypeMatcher implements FieldMatcher {
    // 使用全限定名以避免与本包的 TypeMatcher 冲突
    private final net.nyana.reflection.type.matcher.TypeMatcher matcher;

    GenericTypeMatcher(net.nyana.reflection.type.matcher.TypeMatcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public boolean matches(Field field) {
        return this.matcher.matches(field.getGenericType());
    }
}
