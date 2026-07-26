package net.nyana.reflection.condition;

import net.nyana.reflection.condition.annotation.Condition;
import net.nyana.reflection.exception.ReflectionException;

import java.lang.reflect.Constructor;

/**
 * 求值代理注解上声明的条件
 */
public final class ConditionChecker {
    private ConditionChecker() {
    }

    /**
     * 仅当全部条件都接受各自的值时返回 {@code true}
     */
    public static boolean matches(Condition[] conditions) {
        for (Condition declaration : conditions) {
            Class<? extends CustomCondition> type = declaration.type();
            try {
                Constructor<? extends CustomCondition> constructor = type.getDeclaredConstructor();
                if (!constructor.canAccess(null) && !constructor.trySetAccessible()) {
                    throw new ReflectionException("Cannot access condition constructor: " + type.getName());
                }
                CustomCondition condition = constructor.newInstance();
                if (!condition.check(declaration.value())) {
                    return false;
                }
            } catch (ReflectiveOperationException e) {
                throw new ReflectionException(
                        "Condition must declare a no-argument constructor: " + type.getName(),
                        e
                );
            }
        }
        return true;
    }
}
