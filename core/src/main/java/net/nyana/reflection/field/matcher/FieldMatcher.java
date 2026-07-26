package net.nyana.reflection.field.matcher;

import java.lang.reflect.Field;

// 字段匹配器: 既可作为函数式判断, 也能通过 and/or/not 组合, 并提供按名称/类型/修饰符筛选的工厂方法.
public interface FieldMatcher {

    // 判断给定字段是否满足条件.
    boolean matches(final Field field);

    default FieldMatcher or(final FieldMatcher matcher) {
        return field -> matches(field) || matcher.matches(field);
    }

    default FieldMatcher and(final FieldMatcher matcher) {
        return field -> matches(field) && matcher.matches(field);
    }

    static FieldMatcher any() {
        return field -> true;
    }

    static FieldMatcher anyOf(final FieldMatcher... matchers) {
        return field -> {
            // 任一匹配器命中即算匹配(逻辑或)
            for (final FieldMatcher matcher : matchers) {
                if (matcher.matches(field)) {
                    return true;
                }
            }
            return false;
        };
    }

    static FieldMatcher allOf(final FieldMatcher... matchers) {
        return field -> {
            // 需全部匹配器命中才算匹配(逻辑与)
            for (final FieldMatcher matcher : matchers) {
                if (!matcher.matches(field)) {
                    return false;
                }
            }
            return true;
        };
    }

    static FieldMatcher not(final FieldMatcher matcher) {
        return field -> !matcher.matches(field);
    }

    static FieldMatcher noneOf(final FieldMatcher... matchers) {
        return not(anyOf(matchers));
    }

    static FieldMatcher named(String name) {
        return new NameMatcher(name, true);
    }

    static FieldMatcher namedNoRemap(String name) {
        return new NameMatcher(name, false);
    }

    static FieldMatcher named(String... names) {
        if (names.length == 1) {
            return named(names[0]);
        }
        return new NamesMatcher(names, true);
    }

    static FieldMatcher namedNoRemap(String... names) {
        if (names.length == 1) {
            return namedNoRemap(names[0]);
        }
        return new NamesMatcher(names, false);
    }

    static FieldMatcher type(Class<?> clazz) {
        return new TypeMatcher(clazz);
    }

    static FieldMatcher type(net.nyana.reflection.type.matcher.TypeMatcher typeMatcher) {
        return new GenericTypeMatcher(typeMatcher);
    }

    static FieldMatcher privateField() {
        return PrivateMatcher.INSTANCE;
    }

    static FieldMatcher publicField() {
        return PublicMatcher.INSTANCE;
    }

    static FieldMatcher protectedField() {
        return ProtectedMatcher.INSTANCE;
    }

    static FieldMatcher staticField() {
        return StaticMatcher.INSTANCE;
    }

    static FieldMatcher instanceField() {
        return InstanceMatcher.INSTANCE;
    }

    static FieldMatcher finalField() {
        return FinalMatcher.INSTANCE;
    }
}
