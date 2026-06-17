package net.nyana.reflection.type.matcher;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

// 泛型数组类型匹配器: 匹配 T[] 形式的泛型数组, 并对其元素类型递归套用 element 匹配器.
final class GenericArrayMatcher implements TypeMatcher {
    private final TypeMatcher element; // 数组元素类型的匹配器

    GenericArrayMatcher(TypeMatcher element) {
        this.element = element;
    }

    @Override
    public boolean matches(Type type) {
        // 仅匹配泛型数组类型, 再对元素类型递归匹配
        if (!(type instanceof GenericArrayType genericArrayType)) {
            return false;
        }
        return this.element.matches(genericArrayType.getGenericComponentType());
    }
}
