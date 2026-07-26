package net.nyana.reflection.type.matcher;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

// 参数化类型匹配器: 匹配 List<String> 等参数化类型, 可分别校验原始类型/所属外部类型/各实际类型参数.
final class ParameterizedMatcher implements TypeMatcher {
    private final TypeMatcher rawMatcher; // 原始类型(如 List)的匹配器
    private final TypeMatcher ownerMatcher; // 外部所属类型的匹配器(用于内部类)
    private final TypeMatcher[] matchers; // 各实际类型参数的匹配器

    ParameterizedMatcher(@Nullable TypeMatcher rawMatcher, @Nullable TypeMatcher ownerMatcher, @Nullable TypeMatcher... matchers) {
        this.rawMatcher = rawMatcher;
        this.ownerMatcher = ownerMatcher;
        this.matchers = matchers;
    }

    @Override
    public boolean matches(Type type) {
        if (!(type instanceof ParameterizedType parameterizedType)) {
            return false;
        }

        // 校验原始类型(如 List<String> 中的 List)
        Type rawType = parameterizedType.getRawType();
        if (this.rawMatcher != null) {
            if (!this.rawMatcher.matches(rawType)) {
                return false;
            }
        }

        // 校验外部所属类型(内部类场景)
        if (this.ownerMatcher != null) {
            Type ownerType = parameterizedType.getOwnerType();
            if (!this.ownerMatcher.matches(ownerType)) {
                return false;
            }
        }

        // 逐个校验实际类型参数, 个数需一致
        if (this.matchers != null && this.matchers.length > 0) {
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length != matchers.length) {
                return false;
            }
            for (int i = 0; i < actualTypeArguments.length; i++) {
                if (!this.matchers[i].matches(actualTypeArguments[i])) {
                    return false;
                }
            }
        }
        return true;
    }
}
