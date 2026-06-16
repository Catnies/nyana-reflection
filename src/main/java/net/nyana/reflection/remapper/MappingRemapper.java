package net.nyana.reflection.remapper;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.nyana.reflection.util.StringPool;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class MappingRemapper implements Remapper {
    private final Map<String, ClassMapping> byTargetClassName;
    private final Map<String, ClassMapping> bySourceClassName;

    // 根据映射数据直接创建 Remapper.
    MappingRemapper(Map<String, ClassMapping> bySourceClassName, Map<String, ClassMapping> byTargetClassName) {
        this.byTargetClassName = byTargetClassName;
        this.bySourceClassName = bySourceClassName;
    }

    // 读取通用 MappingsFile, 解析并接创建 Remapper.
    MappingRemapper(InputStream in, String sourceNamespace, String targetNamespace) throws IOException {
        // 解析 mappings
        MemoryMappingTree tree = new MemoryMappingTree(true);
        tree.setSrcNamespace(sourceNamespace);
        tree.setDstNamespaces(List.of(targetNamespace));
        MappingReader.read(new InputStreamReader(in, StandardCharsets.UTF_8), tree);

        // 遍历每个类,构建 sourceClassName <-> targetClassName 双向索引
        StringPool pool = new StringPool();
        Map<String, ClassMapping> byTargetClassName = new HashMap<>();
        Map<String, ClassMapping> bySourceClassName = new HashMap<>();
        for (MappingTree.ClassMapping mapping : tree.getClasses()) {
            Map<String, String> fields = new HashMap<>();
            for (MappingTree.FieldMapping field : mapping.getFields()) {
                String sourceField = field.getName(sourceNamespace);
                String targetField = field.getName(targetNamespace);
                fields.put(pool.intern(sourceField), pool.intern(targetField));
            }

            Map<String, String> methods = new HashMap<>();
            for (MappingTree.MethodMapping method : mapping.getMethods()) {
                String sourceName = Objects.requireNonNull(method.getName(sourceNamespace));
                String targetName = Objects.requireNonNull(method.getName(targetNamespace));
                String desc = Objects.requireNonNull(method.getDesc(targetNamespace));
                // 例如  "setDayTime"+"J" = "setDayTimeJ" → 查表得 "a"
                methods.put(
                        pool.intern(methodKey(sourceName, desc)),    // 无混淆名 + 混淆参数
                        pool.intern(targetName)                      // 混淆名
                );
            }

            String deobfClassName = Objects.requireNonNull(mapping.getName(sourceNamespace)).replace('/', '.');
            String obfClassName = Objects.requireNonNull(mapping.getName(targetNamespace)).replace('/', '.');
            ClassMapping classMapping = new ClassMapping(deobfClassName, obfClassName, fields, methods);
            bySourceClassName.put(deobfClassName, classMapping);
            byTargetClassName.put(obfClassName, classMapping);
        }

        this.byTargetClassName = byTargetClassName;
        this.bySourceClassName = bySourceClassName;
    }

    @Override
    public String remapClassName(String className) {
        ClassMapping classMapping = this.bySourceClassName.get(className);
        if (classMapping == null) {
            return className;
        }
        return classMapping.targetClassName();
    }

    @Override
    public String remapFieldName(Class<?> clazz, String fieldName) {
        ClassMapping classMapping = this.byTargetClassName.get(clazz.getName());
        if (classMapping == null) {
            return fieldName;
        }
        return classMapping.fieldsBySourceName().getOrDefault(fieldName, fieldName);
    }

    @Override
    public String remapMethodName(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        ClassMapping classMapping = this.byTargetClassName.get(clazz.getName());
        if (classMapping == null) {
            return methodName;
        }
        return classMapping.methodsBySourceNameAndTargetParams().getOrDefault(methodKey(methodName, parameterTypes), methodName);
    }

    // 根据 方法名称 + 方法参数类型 生成一份 MethodKey
    private static String methodKey(final String deobfName, final Class<?>... paramTypes) {
        final StringBuilder builder = new StringBuilder().append(deobfName);
        for (final Class<?> param : paramTypes) {
            builder.append(param.descriptorString());
        }
        return builder.toString();
    }

    // 根据 方法名称 + 方法参数类型描述 生成一份 MethodKey
    private static String methodKey(final String deobfName, final String obfMethodDesc) {
        String ret = obfMethodDesc.substring(1);
        ret = ret.substring(0, ret.indexOf(")"));
        return deobfName + ret;
    }
}
