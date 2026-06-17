package net.nyana.reflection.method.matcher;

import java.lang.reflect.Method;

// 方法参数类型匹配器: 方法的参数类型按顺序与目标类型逐一精确相等时匹配.
final class TakeArgumentsMatcher implements MethodMatcher {
    private final Class<?>[] arguments; // 期望的参数类型列表

    TakeArgumentsMatcher(Class<?>... arguments) {
        this.arguments = arguments;
    }

    @Override
    public boolean matches(Method method) {
        // 参数个数不一致直接排除
        Class<?>[] params = method.getParameterTypes();
        if (params.length != arguments.length) {
            return false;
        }

        // 逐个比对参数类型, 要求完全相等
        for (int i = 0; i < this.arguments.length; i++) {
            if (this.arguments[i] != params[i]) {
                return false;
            }
        }
        return true;
    }
}
