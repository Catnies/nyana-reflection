package net.nyana.reflection.constructor.matcher;

import net.nyana.reflection.type.matcher.TypeMatcher;

import java.lang.reflect.Constructor;

// 构造器匹配器: 既可作为函数式判断, 也能通过 and/or/not 组合, 并提供按参数/修饰符筛选的工厂方法.
public interface ConstructorMatcher {

    // 判断给定构造器是否满足条件.
    boolean matches(final Constructor<?> constructor);

    default ConstructorMatcher or(final ConstructorMatcher matcher) {
        return constructor -> this.matches(constructor) || matcher.matches(constructor);
    }

    default ConstructorMatcher and(final ConstructorMatcher matcher) {
        return constructor -> this.matches(constructor) && matcher.matches(constructor);
    }

    static ConstructorMatcher any() {
        return constructor -> true;
    }

    static ConstructorMatcher anyOf(final ConstructorMatcher... matchers) {
        return constructor -> {
            // 任一匹配器命中即算匹配(逻辑或)
            for (final ConstructorMatcher matcher : matchers) {
                if (matcher.matches(constructor)) {
                    return true;
                }
            }
            return false;
        };
    }

    static ConstructorMatcher allOf(final ConstructorMatcher... matchers) {
        return constructor -> {
            // 需全部匹配器命中才算匹配(逻辑与)
            for (final ConstructorMatcher matcher : matchers) {
                if (!matcher.matches(constructor)) {
                    return false;
                }
            }
            return true;
        };
    }

    static ConstructorMatcher not(final ConstructorMatcher matcher) {
        return constructor -> !matcher.matches(constructor);
    }

    static ConstructorMatcher noneOf(final ConstructorMatcher... matchers) {
        return not(anyOf(matchers));
    }

    static ConstructorMatcher takeArguments(final Class<?>... types) {
        return new TakeArgumentsMatcher(types);
    }

    static ConstructorMatcher takeArguments(final TypeMatcher... matchers) {
        return new TakeGenericArgumentsMatcher(matchers);
    }

    static ConstructorMatcher takeArgument(final int index, final Class<?> type) {
        return new TakeArgumentMatcher(index, type);
    }

    static ConstructorMatcher takeArgument(final int index, final TypeMatcher matcher) {
        return new TakeGenericArgumentMatcher(index, matcher);
    }

    static ConstructorMatcher privateConstructor() {
        return PrivateMatcher.INSTANCE;
    }

    static ConstructorMatcher publicConstructor() {
        return PublicMatcher.INSTANCE;
    }

    static ConstructorMatcher protectedConstructor() {
        return ProtectedMatcher.INSTANCE;
    }
}
