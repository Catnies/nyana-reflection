package net.nyana.reflection.type.matcher;

import java.lang.reflect.Type;

// 泛型类型匹配器: 用于匹配 java.lang.reflect.Type 的结构(参数化类型/通配符/类型变量/泛型数组等), 支持 and/or/not 组合.
public interface TypeMatcher {

    // 判断给定类型是否满足条件.
    boolean matches(final Type type);

    default TypeMatcher or(final TypeMatcher matcher) {
        return type -> matches(type) || matcher.matches(type);
    }

    default TypeMatcher and(final TypeMatcher matcher) {
        return type -> matches(type) && matcher.matches(type);
    }

    static TypeMatcher any() {
        return type -> true;
    }

    static TypeMatcher anyOf(final TypeMatcher... matchers) {
        return type -> {
            // 任一匹配器命中即算匹配(逻辑或)
            for (final TypeMatcher matcher : matchers) {
                if (matcher.matches(type)) {
                    return true;
                }
            }
            return false;
        };
    }

    static TypeMatcher allOf(final TypeMatcher... matchers) {
        return type -> {
            // 需全部匹配器命中才算匹配(逻辑与)
            for (final TypeMatcher matcher : matchers) {
                if (!matcher.matches(type)) {
                    return false;
                }
            }
            return true;
        };
    }

    static TypeMatcher not(final TypeMatcher matcher) {
        return type -> !matcher.matches(type);
    }

    static TypeMatcher noneOf(final TypeMatcher... matchers) {
        return not(anyOf(matchers));
    }

    static TypeMatcher parameterized(final TypeMatcher raw, TypeMatcher[] parameters) {
        return new ParameterizedMatcher(raw, null, parameters);
    }

    static TypeMatcher parameterized(final TypeMatcher raw, TypeMatcher owner, TypeMatcher... parameters) {
        return new ParameterizedMatcher(raw, owner, parameters);
    }

    static TypeMatcher genericArray(final TypeMatcher component) {
        return new GenericArrayMatcher(component);
    }

    static TypeMatcher wildcard() {
        return WildCardMatcher.SIMPLE;
    }

    static TypeMatcher wildcard(final TypeMatcher[] upper, final TypeMatcher[] lower) {
        return new WildCardMatcher(upper, lower);
    }

    static TypeMatcher wildcardUpper(final TypeMatcher... wildcard) {
        return new WildCardMatcher(wildcard, null);
    }

    static TypeMatcher wildcardLower(final TypeMatcher... wildcard) {
        return new WildCardMatcher(null, wildcard);
    }

    static TypeMatcher typeVariable() {
        return TypeVariableMatcher.SIMPLE;
    }

    static TypeMatcher typeVariable(final String name) {
        return new TypeVariableMatcher(name, null);
    }

    static TypeMatcher typeVariable(final String name, final TypeMatcher... bounds) {
        return new TypeVariableMatcher(name, bounds);
    }

    static TypeMatcher clazz(final Class<?> clazz) {
        return new ClassMatcher(clazz);
    }
}
