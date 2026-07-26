package net.nyana.reflection.method.matcher;

import net.nyana.reflection.NyanaReflection;

import java.lang.reflect.Method;

final class NameMatcher implements MethodMatcher {
    private final String name;
    private final boolean remap;

    NameMatcher(String name, boolean remap) {
        this.name = name;
        this.remap = remap;
    }

    @Override
    public boolean matches(Method method) {
        if (!this.remap || NyanaReflection.getRemapper().isNoOp()) {
            return method.getName().equals(this.name);
        } else {
            Class<?> declaringClass = method.getDeclaringClass();
            Class<?>[] parameterTypes = method.getParameterTypes();
            return method.getName().equals(NyanaReflection.getRemapper().remapMethodName(declaringClass, this.name, parameterTypes));
        }
    }
}
