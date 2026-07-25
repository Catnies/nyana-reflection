package net.nyana.reflection.minecraft.remapper;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.nyana.reflection.remapper.Remapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public final class PaperRemapper implements Remapper {
    private final Map<String, ClassData> byObf;
    private final Map<String, ClassData> byDeobf;

    PaperRemapper(Map<String, ClassData> byDeobf,
                 Map<String, ClassData> byObf) {
        this.byObf = byObf;
        this.byDeobf = byDeobf;
    }

    PaperRemapper(InputStream in, String deobf, String obf) throws IOException {
        MemoryMappingTree tree = new MemoryMappingTree(true);
        tree.setSrcNamespace(deobf);
        tree.setDstNamespaces(List.of(obf));
        MappingReader.read(new InputStreamReader(in, StandardCharsets.UTF_8), tree);
        StringPool pool = new StringPool(new HashMap<>(4096, 0.5f));
        Map<String, ClassData> byObf = new HashMap<>();
        Map<String, ClassData> byDeobf = new HashMap<>();
        for (MappingTree.ClassMapping mapping : tree.getClasses()) {
            Map<String, String> fields = new HashMap<>();
            for (MappingTree.FieldMapping field : mapping.getFields()) {
                fields.put(pool.get(field.getName(deobf)), pool.get(field.getName(obf)));
            }
            Map<String, String> methods = new HashMap<>();
            for (MappingTree.MethodMapping method : mapping.getMethods()) {
                methods.put(pool.get(methodKey(requireNonNull(method.getName(deobf)), requireNonNull(method.getDesc(obf)))),
                        pool.get(requireNonNull(method.getName(obf))));
            }
            String obfClassName = requireNonNull(mapping.getName(obf)).replace('/', '.');
            String deobfClassName = requireNonNull(mapping.getName(deobf)).replace('/', '.');
            ClassData classData = new ClassData(deobfClassName, obfClassName, fields, methods);
            byObf.put(obfClassName, classData);
            byDeobf.put(deobfClassName, classData);
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

    private static String methodKey(final String deobfName, final Class<?>... paramTypes) {
        return deobfName + paramsDescriptor(paramTypes);
    }

    private static String methodKey(final String deobfName, final String obfMethodDesc) {
        return deobfName + paramsDescFromMethodDesc(obfMethodDesc);
    }

    private static String paramsDescriptor(final Class<?>... params) {
        final StringBuilder builder = new StringBuilder();
        for (final Class<?> param : params) {
            builder.append(param.descriptorString());
        }
        return builder.toString();
    }

    private static String paramsDescFromMethodDesc(final String methodDescriptor) {
        String ret = methodDescriptor.substring(1);
        ret = ret.substring(0, ret.indexOf(")"));
        return ret;
    }
}