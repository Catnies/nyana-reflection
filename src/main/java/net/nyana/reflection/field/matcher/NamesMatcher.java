package net.nyana.reflection.field.matcher;

import net.nyana.reflection.NyanaReflection;

import java.lang.reflect.Field;

// 多名称字段匹配器: 字段名命中任一候选名即匹配, remap 为 true 时先把候选名映射为运行时名称.
final class NamesMatcher implements FieldMatcher {
    private final String[] names; // 候选字段名
    private final boolean remap; // 是否对候选名做 Remap 映射

    NamesMatcher(String[] names, boolean remap) {
        this.names = names;
        this.remap = remap;
    }

    @Override
    public boolean matches(Field field) {
        String fieldName = field.getName();

        // 无需 remap 或 Remapper 为空操作时, 直接按原名比对
        if (!this.remap || NyanaReflection.getRemapper().isNoOp()) {
            for (String name : this.names) {
                if (fieldName.equals(name)) {
                    return true;
                }
            }
        } else {
            // 否则将每个候选名映射为运行时字段名后再比对
            Class<?> clazz = field.getDeclaringClass();
            for (String name : this.names) {
                if (fieldName.equals(NyanaReflection.getRemapper().remapFieldName(clazz, name))) {
                    return true;
                }
            }
        }
        return false;
    }
}
