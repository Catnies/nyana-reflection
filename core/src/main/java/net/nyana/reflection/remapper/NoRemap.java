package net.nyana.reflection.remapper;

/**
 * 空映射实现, 不重映射输入的类名和字段.
 */
final class NoRemap implements Remapper {
    public static final NoRemap INSTANCE = new NoRemap();

    private NoRemap() {}

    @Override
    public String remapClassName(String className) {
        return className;
    }

    @Override
    public String remapFieldName(Class<?> clazz, String fieldName) {
        return fieldName;
    }

    @Override
    public String remapMethodName(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        return methodName;
    }

    @Override
    public String remapClassOrArrayName(String clazzOrArrayName) {
        return clazzOrArrayName;
    }

    @Override
    public boolean isNoOp() {
        return true;
    }
}
