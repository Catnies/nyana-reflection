package net.nyana.reflection.condition.impl;

import net.nyana.reflection.condition.CustomCondition;
import org.jetbrains.annotations.NotNull;

/**
 * 在不初始化目标类的情况下检查它是否可被解析
 */
public final class ClassExistsCondition extends CustomCondition {
    private static boolean isPresent(String className, ClassLoader classLoader) {
        try {
            Class.forName(className, false, classLoader);
            return true;
        } catch (ClassNotFoundException | LinkageError ignored) {
            return false;
        }
    }

    @Override
    public boolean check(@NotNull String[] value) {
        if (value.length == 0) {
            return false;
        }

        String className = value[0];
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (isPresent(className, contextClassLoader)) {
            return true;
        }

        ClassLoader ownClassLoader = ClassExistsCondition.class.getClassLoader();
        return ownClassLoader != contextClassLoader && isPresent(className, ownClassLoader);
    }
}
