package net.nyana.reflection.proxy;

import net.nyana.reflection.NyanaReflection;
import net.nyana.reflection.util.ASMUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 基于 ProxyDefinition 写入 ASM 隐藏类字节码
 */
final class ProxyClassWriter implements Opcodes {

    /**
     * 使用 ClassWriter 根据 ProxyDefinition 编写一个实现代理接口的隐藏类, 并收集需要运行时回填的 MethodHandle
     */
    ProxyClassBytes write(ProxyDefinition definition) {
        String internalName = Type.getInternalName(definition.targetType()) + "$Proxy";
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classWriter.visit(
                V17,
                ACC_PUBLIC | ACC_FINAL,
                internalName,
                null,
                "java/lang/Object",
                new String[]{Type.getInternalName(definition.proxyType())}
        );

        // 每个代理类都只需要一个无参构造器, 状态全部由调用参数提供
        {
            MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }

        // 根据 binding 的数据处理字节码生成
        EmissionContext ctx = new EmissionContext(classWriter, internalName);
        List<ProxyBinding> bindings = definition.bindings();
        for (int i = 0; i < bindings.size(); i++) {
            writeBinding(ctx, bindings.get(i));
        }

        classWriter.visitEnd();
        return new ProxyClassBytes(classWriter.toByteArray(), List.copyOf(ctx.staticHandleBindings));
    }

    // 根据 ProxyBinding 编写方法实现
    private static void writeBinding(EmissionContext ctx, ProxyBinding binding) {
        if (binding instanceof ProxyBinding.FieldGetter getField) {
            writeFieldGetter(ctx, getField.proxyMethod(), getField.targetField());
            return;
        }
        if (binding instanceof ProxyBinding.FieldSetter setField) {
            writeFieldSetter(ctx, setField.proxyMethod(), setField.targetField());
            return;
        }
        if (binding instanceof ProxyBinding.MethodInvoker invokeMethod) {
            writeMethod(ctx, invokeMethod.proxyMethod(), invokeMethod.targetMethod());
            return;
        }
        if (binding instanceof ProxyBinding.ConstructorInvoker invokeConstructor) {
            writeConstructor(ctx, invokeConstructor.proxyMethod(), invokeConstructor.targetConstructor());
        }
    }

    /**
     * 写入字段 getter, 兼容目标字段基本类型装箱到代理方法返回 Object 的场景
     */
    private static void writeFieldGetter(EmissionContext ctx, Method method, Field field) {
        if (ctx.hasWritten(method)) {
            return;
        }

        MethodVisitor mv = ctx.classWriter.visitMethod(ACC_PUBLIC, method.getName(), Type.getMethodDescriptor(method), null, null);
        mv.visitCode();

        Class<?> fieldType = field.getType();
        Class<?> returnType = method.getReturnType();
        String owner = Type.getInternalName(field.getDeclaringClass());
        String fieldDescriptor = Type.getDescriptor(fieldType);

        // 实例字段需从第一个参数加载目标对象, 静态字段则直接 GETSTATIC
        if (!Modifier.isStatic(field.getModifiers())) {
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, owner);
            mv.visitFieldInsn(GETFIELD, owner, field.getName(), fieldDescriptor);
        } else {
            mv.visitFieldInsn(GETSTATIC, owner, field.getName(), fieldDescriptor);
        }

        // 将真实字段类型转换为代理接口声明的返回类型
        writeGetterReturnCast(mv, method, field, fieldType, returnType, fieldDescriptor);
        mv.visitInsn(Type.getType(returnType).getOpcode(IRETURN));

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    /**
     * 写入普通字段 setter, final 字段会切换到 MethodHandle 写入路径
     */
    private static void writeFieldSetter(EmissionContext ctx, Method method, Field field) {
        if (ctx.hasWritten(method)) {
            return;
        }
        if (Modifier.isFinal(field.getModifiers())) {
            writeFinalFieldSetter(ctx, method, field);
            return;
        }

        MethodVisitor mv = ctx.classWriter.visitMethod(ACC_PUBLIC, method.getName(), Type.getMethodDescriptor(method), null, null);
        mv.visitCode();

        Class<?> fieldType = field.getType();
        String owner = Type.getInternalName(field.getDeclaringClass());
        String fieldDescriptor = Type.getDescriptor(fieldType);

        // 实例字段需要先压入 owner, 写入值位于第二个参数槽位
        if (!Modifier.isStatic(field.getModifiers())) {
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, owner);
        }

        int valueParamIndex = Modifier.isStatic(field.getModifiers()) ? 1 : 2;
        loadAndCastSetterValue(mv, method, field, valueParamIndex);

        if (!Modifier.isStatic(field.getModifiers())) {
            mv.visitFieldInsn(PUTFIELD, owner, field.getName(), fieldDescriptor);
        } else {
            mv.visitFieldInsn(PUTSTATIC, owner, field.getName(), fieldDescriptor);
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    /**
     * final 字段不能稳定地使用 PUTFIELD 修改, 这里通过静态 MethodHandle 槽位延迟绑定 setter
     */
    private static void writeFinalFieldSetter(EmissionContext ctx, Method method, Field field) {
        String handleFieldName = "HANDLE_" + ctx.staticHandleBindings.size();
        ctx.classWriter.visitField(ACC_PRIVATE | ACC_STATIC, handleFieldName, "Ljava/lang/invoke/MethodHandle;", null, null);

        MethodVisitor mv = ctx.classWriter.visitMethod(ACC_PUBLIC, method.getName(), Type.getMethodDescriptor(method), null, null);
        mv.visitCode();

        // 加载运行时注入的 MethodHandle, 再按 invokeExact 描述符压入 owner/value
        mv.visitFieldInsn(GETSTATIC, ctx.internalName, handleFieldName, "Ljava/lang/invoke/MethodHandle;");

        boolean isStatic = Modifier.isStatic(field.getModifiers());
        if (!isStatic) {
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(field.getDeclaringClass()));
        }

        int valueParamIndex = isStatic ? 1 : 2;
        loadAndCastSetterValue(mv, method, field, valueParamIndex);

        String fieldDescriptor = Type.getDescriptor(field.getType());
        String invokeDesc = isStatic
                ? "(" + fieldDescriptor + ")V"
                : "(" + Type.getDescriptor(field.getDeclaringClass()) + fieldDescriptor + ")V";
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "invokeExact", invokeDesc, false);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        MethodHandle handle = NyanaReflection.unreflectSetter(field);
        ctx.staticHandleBindings.add(handle);
    }

    /**
     * 写入目标方法调用, 实例方法约定代理方法第一个参数为目标对象
     */
    private static void writeMethod(EmissionContext ctx, Method proxyMethod, Method targetMethod) {
        if (ctx.hasWritten(proxyMethod)) {
            return;
        }

        MethodVisitor mv = ctx.classWriter.visitMethod(ACC_PUBLIC, proxyMethod.getName(), Type.getMethodDescriptor(proxyMethod), null, null);
        mv.visitCode();

        Class<?> owner = targetMethod.getDeclaringClass();
        Class<?>[] targetParamTypes = targetMethod.getParameterTypes();
        boolean isStaticTarget = Modifier.isStatic(targetMethod.getModifiers());

        // 实例调用先加载 receiver, 参数槽位从 2 开始; 静态调用没有 receiver, 从 1 开始
        if (!isStaticTarget) {
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(owner));
        }

        int currentSlot = isStaticTarget ? 1 : 2;
        for (Class<?> targetParamType : targetParamTypes) {
            Type asmType = Type.getType(targetParamType);
            mv.visitVarInsn(asmType.getOpcode(ILOAD), currentSlot);
            if (!targetParamType.isPrimitive()) {
                mv.visitTypeInsn(CHECKCAST, Type.getInternalName(targetParamType));
            }
            currentSlot += asmType.getSize();
        }

        int opcode = isStaticTarget ? INVOKESTATIC : (owner.isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL);
        mv.visitMethodInsn(
                opcode,
                Type.getInternalName(owner),
                targetMethod.getName(),
                Type.getMethodDescriptor(targetMethod),
                owner.isInterface()
        );

        writeReturn(mv, proxyMethod, targetMethod);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    /**
     * 写入目标构造器调用, 返回值始终是新建出的目标对象
     */
    private static void writeConstructor(EmissionContext ctx, Method method, Constructor<?> constructor) {
        if (ctx.hasWritten(method)) {
            return;
        }

        MethodVisitor mv = ctx.classWriter.visitMethod(ACC_PUBLIC, method.getName(), Type.getMethodDescriptor(method), null, null);
        mv.visitCode();

        Class<?> owner = constructor.getDeclaringClass();
        String internalName = Type.getInternalName(owner);
        Class<?>[] targetParamTypes = constructor.getParameterTypes();

        mv.visitTypeInsn(NEW, internalName);
        mv.visitInsn(DUP);

        int currentSlot = 1;
        for (Class<?> targetParamType : targetParamTypes) {
            Type asmType = Type.getType(targetParamType);
            mv.visitVarInsn(asmType.getOpcode(ILOAD), currentSlot);
            if (!targetParamType.isPrimitive()) {
                mv.visitTypeInsn(CHECKCAST, Type.getInternalName(targetParamType));
            }
            currentSlot += asmType.getSize();
        }

        mv.visitMethodInsn(
                INVOKESPECIAL,
                internalName,
                "<init>",
                Type.getConstructorDescriptor(constructor),
                false
        );
        mv.visitInsn(ARETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void writeGetterReturnCast(
            MethodVisitor mv,
            Method method,
            Field field,
            Class<?> fieldType,
            Class<?> returnType,
            String fieldDescriptor
    ) {
        if (returnType.isPrimitive()) {
            if (!fieldType.isPrimitive()) {
                throw new IllegalArgumentException(String.format(
                        "Cannot unbox object field '%s' (%s) to primitive return type '%s' in method '%s'",
                        field.getName(), fieldType.getSimpleName(), returnType.getSimpleName(), method.getName()
                ));
            }
            if (fieldType != returnType) {
                throw new IllegalArgumentException(String.format(
                        "Incompatible primitive types in method '%s': cannot return field '%s' of type '%s' as '%s'",
                        method.getName(), field.getName(), fieldType.getSimpleName(), returnType.getSimpleName()
                ));
            }
            return;
        }

        if (fieldType.isPrimitive()) {
            ASMUtils.box(mv, fieldDescriptor);
        } else {
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(returnType));
        }
    }

    private static void loadAndCastSetterValue(
            MethodVisitor mv,
            Method method,
            Field field,
            int valueParamIndex
    ) {
        Class<?> fieldType = field.getType();
        Class<?> proxyParamType = method.getParameterTypes()[valueParamIndex - 1];
        Type asmProxyParamType = Type.getType(proxyParamType);
        mv.visitVarInsn(asmProxyParamType.getOpcode(ILOAD), valueParamIndex);

        if (fieldType.isPrimitive()) {
            if (!proxyParamType.isPrimitive()) {
                ASMUtils.unboxAndCast(mv, Type.getDescriptor(fieldType));
                return;
            }
            if (proxyParamType != fieldType) {
                throw new IllegalArgumentException(String.format(
                        "Primitive type mismatch in method '%s': cannot pass '%s' to field '%s' of type '%s' without explicit conversion",
                        method.getName(),
                        proxyParamType.getSimpleName(),
                        field.getName(),
                        fieldType.getSimpleName()
                ));
            }
            return;
        }

        mv.visitTypeInsn(CHECKCAST, Type.getInternalName(fieldType));
    }

    private static void writeReturn(MethodVisitor mv, Method proxyMethod, Method targetMethod) {
        Class<?> targetReturnType = targetMethod.getReturnType();
        Class<?> proxyReturnType = proxyMethod.getReturnType();

        if (targetReturnType == void.class) {
            if (proxyReturnType != void.class) {
                mv.visitInsn(ACONST_NULL);
                mv.visitInsn(ARETURN);
            } else {
                mv.visitInsn(RETURN);
            }
            return;
        }

        if (targetReturnType.isPrimitive()) {
            writePrimitiveReturn(mv, targetReturnType, proxyReturnType);
        } else {
            writeObjectReturn(mv, targetReturnType, proxyReturnType);
        }
    }

    private static void writePrimitiveReturn(
            MethodVisitor mv,
            Class<?> targetReturnType,
            Class<?> proxyReturnType
    ) {
        if (!proxyReturnType.isPrimitive()) {
            ASMUtils.box(mv, Type.getDescriptor(targetReturnType));
            mv.visitInsn(ARETURN);
            return;
        }
        if (targetReturnType != proxyReturnType) {
            throw new IllegalArgumentException("Primitive mismatch: " + targetReturnType + " to " + proxyReturnType);
        }
        mv.visitInsn(Type.getType(proxyReturnType).getOpcode(IRETURN));
    }

    private static void writeObjectReturn(
            MethodVisitor mv,
            Class<?> targetReturnType,
            Class<?> proxyReturnType
    ) {
        if (proxyReturnType.isPrimitive()) {
            ASMUtils.unboxAndCast(mv, Type.getDescriptor(proxyReturnType));
            mv.visitInsn(Type.getType(proxyReturnType).getOpcode(IRETURN));
            return;
        }
        if (!proxyReturnType.isAssignableFrom(targetReturnType)) {
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(proxyReturnType));
        }
        mv.visitInsn(ARETURN);
    }

    private static final class EmissionContext {
        private final ClassWriter classWriter; // 当前代理类的 ClassWriter
        private final String internalName; // 当前代理类内部名, 用于访问自身静态字段
        private final Set<String> writtenSignatures = new HashSet<>(); // 避免父子接口重复写入同一签名
        private final List<MethodHandle> staticHandleBindings = new ArrayList<>(4); // HANDLE_n 静态槽位绑定值

        private EmissionContext(ClassWriter classWriter, String internalName) {
            this.classWriter = classWriter;
            this.internalName = internalName;
        }

        private boolean hasWritten(Method method) {
            String signature = method.getName() + Type.getMethodDescriptor(method);
            return !this.writtenSignatures.add(signature);
        }
    }
}
