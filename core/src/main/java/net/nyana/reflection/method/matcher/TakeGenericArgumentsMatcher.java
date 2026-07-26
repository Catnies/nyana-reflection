package net.nyana.reflection.method.matcher;

import net.nyana.reflection.type.matcher.TypeMatcher;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

// 方法泛型参数匹配器: 方法的泛型参数类型按顺序交给各自的类型匹配器判定, 全部命中才匹配.
final class TakeGenericArgumentsMatcher implements MethodMatcher {
    private final TypeMatcher[] arguments; // 各参数位置对应的类型匹配器

    TakeGenericArgumentsMatcher(TypeMatcher... arguments) {
        this.arguments = arguments;
    }

    @Override
    public boolean matches(Method method) {
        // 参数个数不一致直接排除
        Type[] params = method.getGenericParameterTypes();
        if (params.length != arguments.length) {
            return false;
        }

        // 逐个用对应的类型匹配器校验泛型参数
        for (int i = 0; i < this.arguments.length; i++) {
            if (!this.arguments[i].matches(params[i])) {
                return false;
            }
        }
        return true;
    }
}
