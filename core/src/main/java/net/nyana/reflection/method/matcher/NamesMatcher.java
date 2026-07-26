package net.nyana.reflection.method.matcher;

import net.nyana.reflection.NyanaReflection;

import java.lang.reflect.Method;

final class NamesMatcher implements MethodMatcher {
    private final String[] names;
    private final boolean remap;

    NamesMatcher(String[] names, boolean remap) {
        this.names = names;
        this.remap = remap;
    }

    @Override
    public boolean matches(Method method) {
        String methodName = method.getName();
        if (!this.remap || NyanaReflection.getRemapper().isNoOp()) {
            for (String name : this.names) {
                if (methodName.equals(name)) {
                    return true;
                }
            }
        } else {
            Class<?> declaringClass = method.getDeclaringClass();
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (String name : this.names) {
                if (methodName.equals(NyanaReflection.getRemapper().remapMethodName(declaringClass, name, parameterTypes))) {
                    return true;
                }
            }
        }
        return false;
    }
}
