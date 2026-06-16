package net.nyana.reflection.remapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * 将源码侧类名、字段名和方法名映射到当前运行时名称。
 */
public interface Remapper {

    // 无映射单例
    static Remapper noOp() {
        return NoRemap.INSTANCE;
    }

    // 根据 Mappings File 文件创建 Remapper 实例.
    static Remapper loadMappingIo(
            Path mappingsFile,
            String fromNamespace,
            String toNamespace
    ) throws IOException {
        try (InputStream is = Files.newInputStream(mappingsFile)) {
            return Remapper.loadMappingIo(is, fromNamespace, toNamespace);
        }
    }

    // 根据 Mappings 流创建 Remapper 实例.
    static Remapper loadMappingIo(
            InputStream mappingsStream,
            String fromNamespace,
            String toNamespace
    ) throws IOException {
        return new MappingRemapper(mappingsStream, fromNamespace, toNamespace);
    }

    // 将未混淆的 ClassName 映射成混淆后的 ClassName
    String remapClassName(String className);

    // 将未混淆的 FieldName 映射成混淆后的 FieldName
    String remapFieldName(Class<?> owner, String fieldName);

    // 将未混淆的 MethodName 映射成混淆后的 MethodName
    String remapMethodName(Class<?> owner, String methodName, Class<?>... parameterTypes);

    // 将数组类型的 ClassName 映射成混淆后的 ClassName, 例如 "[[Lnet.minecraft.Foo" -> "[[La"
    default String remapClassOrArrayName(final String clazzOrArrayName) {
        if (clazzOrArrayName.isEmpty()) {
            return clazzOrArrayName;
        }
        if (clazzOrArrayName.charAt(0) != '[') {
            return this.remapClassName(clazzOrArrayName);
        }
        final int lastBracketIndex = clazzOrArrayName.lastIndexOf('[');
        if (lastBracketIndex + 1 >= clazzOrArrayName.length()) {
            return clazzOrArrayName;
        }
        if (clazzOrArrayName.charAt(lastBracketIndex + 1) == 'L') {
            if (clazzOrArrayName.charAt(clazzOrArrayName.length() - 1) != ';') {
                return clazzOrArrayName;
            }
            final String className = clazzOrArrayName.substring(lastBracketIndex + 2, clazzOrArrayName.length() - 1);
            final String remappedClassName = this.remapClassName(className);

            return clazzOrArrayName.substring(0, lastBracketIndex + 2) + remappedClassName + ';';
        }
        // 基本类型数组 (int[], boolean[] 等) 不映射.
        return clazzOrArrayName;
    }

    default boolean isNoOp() {
        return false;
    }
}
