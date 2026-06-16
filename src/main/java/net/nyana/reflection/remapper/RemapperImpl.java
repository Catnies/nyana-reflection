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

final class RemapperImpl implements Remapper {
    private final Map<String, ClassData> byObf;
    private final Map<String, ClassData> byDeobf;

    // 根据映射数据直接创建 Remapper.
    RemapperImpl(Map<String, ClassData> byDeobf, Map<String, ClassData> byObf) {
        this.byObf = byObf;
        this.byDeobf = byDeobf;
    }

    // 读取通用 MappingsFile, 解析并接创建 Remapper.
    RemapperImpl(InputStream in, String deobf, String obf) throws IOException {
        // 解析 mappings
        MemoryMappingTree tree = new MemoryMappingTree(true);
        tree.setSrcNamespace(deobf);
        tree.setDstNamespaces(List.of(obf));
        MappingReader.read(new InputStreamReader(in, StandardCharsets.UTF_8), tree);

        // 遍历每个类,构建 deobf <-> obf 双向索引
        StringPool pool = new StringPool();
        Map<String, ClassData> byObf = new HashMap<>();
        Map<String, ClassData> byDeobf = new HashMap<>();
        for (MappingTree.ClassMapping mapping : tree.getClasses()) {
            Map<String, String> fields = new HashMap<>();
            for (MappingTree.FieldMapping field : mapping.getFields()) {
                String deobfField = field.getName(deobf);
                String obfField = field.getName(obf);
                fields.put(pool.intern(deobfField), pool.intern(obfField));
            }

            Map<String, String> methods = new HashMap<>();
            for (MappingTree.MethodMapping method : mapping.getMethods()) {
                String deobfName = Objects.requireNonNull(method.getName(deobf));
                String obfName = Objects.requireNonNull(method.getName(obf));
                String desc = Objects.requireNonNull(method.getDesc(obf));
                // 例如  "setDayTime"+"J" = "setDayTimeJ" → 查表得 "a"
                methods.put(
                        pool.intern(methodKey(deobfName, desc)),    // 无混淆名 + 混淆参数
                        pool.intern(obfName)                        // 混淆名
                );
            }

            String deobfClassName = Objects.requireNonNull(mapping.getName(deobf)).replace('/', '.');
            String obfClassName = Objects.requireNonNull(mapping.getName(obf)).replace('/', '.');
            ClassData classData = new ClassData(deobfClassName, obfClassName, fields, methods);
            byDeobf.put(deobfClassName, classData);
            byObf.put(obfClassName, classData);
        }

        this.byObf = byObf;
        this.byDeobf = byDeobf;
    }

    @Override
    public String remapClassName(String className) {
        ClassData classData = this.byDeobf.get(className);
        if (classData == null) {
            return className;
        }
        return classData.obf();
    }

    @Override
    public String remapFieldName(Class<?> clazz, String fieldName) {
        ClassData classData = this.byObf.get(clazz.getName());
        if (classData == null) {
            return fieldName;
        }
        return classData.fields().getOrDefault(fieldName, fieldName);
    }

    @Override
    public String remapMethodName(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        ClassData classData = this.byObf.get(clazz.getName());
        if (classData == null) {
            return methodName;
        }
        return classData.methods().getOrDefault(methodKey(methodName, parameterTypes), methodName);
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
