package net.nyana.reflection.type.matcher;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

// 类型变量匹配器: 匹配 T/E 等类型变量, 可校验其名称与上界.
final class TypeVariableMatcher implements TypeMatcher {
    public static final TypeVariableMatcher SIMPLE = new TypeVariableMatcher(null, null); // 不约束名称与上界, 只判定是否为类型变量
    private final String name; // 期望的类型变量名, null 表示不校验
    private final TypeMatcher[] bounds; // 上界匹配器, null 表示不校验

    TypeVariableMatcher(@Nullable String name, @Nullable TypeMatcher[] bounds) {
        this.name = name;
        this.bounds = bounds;
    }

    @Override
    public boolean matches(Type type) {
        if (!(type instanceof TypeVariable<?> typeVariable)) {
            return false;
        }

        // 校验类型变量名称
        if (this.name != null) {
            if (!typeVariable.getName().equals(this.name)) {
                return false;
            }
        }

        // 逐个校验上界, 个数需一致
        if (this.bounds != null) {
            Type[] bounds = typeVariable.getBounds();
            if (this.bounds.length != bounds.length) {
                return false;
            }
            for (int i = 0; i < this.bounds.length; i++) {
                if (!this.bounds[i].matches(bounds[i])) {
                    return false;
                }
            }
        }

        return true;
    }
}
