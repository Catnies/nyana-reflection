package net.nyana.reflection.field.factory;

import net.nyana.reflection.NyanaReflection;
import net.nyana.reflection.field.ASMFieldAccessor;
import net.nyana.reflection.util.ASMUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

// 基于 ASM 生成 ASMFieldAccessor 的工厂. 为目标字段动态生成隐藏类, 以 Object 读写字段, 字段值自动装箱/拆箱.
@SuppressWarnings("DuplicatedCode")
final class FieldAccessorFactory implements Opcodes {
    private FieldAccessorFactory() {}
    private static final String ABSTRACT_CLASS_INTERNAL_NAME = Type.getInternalName(ASMFieldAccessor.class); // 生成类继承的抽象基类内部名

    // 读取字段元信息, 生成字节码并定义为宿主类的嵌套隐藏类, 实例化后返回访问器.
    static ASMFieldAccessor create(Field field) throws Exception {
        Class<?> owner = field.getDeclaringClass();
        String fieldName = field.getName();
        String fieldDescriptor = Type.getDescriptor(field.getType());
        boolean isStatic = Modifier.isStatic(field.getModifiers());
        String internalClassName = Type.getInternalName(owner) + "$" + NyanaReflection.getAsmClassPrefix() + "Field_" + fieldName;
        byte[] bytes = generateByteCode(internalClassName, owner, fieldName, fieldDescriptor, isStatic);

        // 借助宿主类的私有 Lookup 定义隐藏类, NESTMATE 使其可访问宿主的私有成员
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(owner, NyanaReflection.getLookup());
        MethodHandles.Lookup hiddenLookup = lookup.defineHiddenClass(bytes, true, MethodHandles.Lookup.ClassOption.NESTMATE);
        return (ASMFieldAccessor) hiddenLookup.lookupClass().getDeclaredConstructor().newInstance();
    }

    // 生成访问器类字节码: 无参构造器 + get(Object)/set(Object,Object) 两个方法.
    private static byte[] generateByteCode(String className, Class<?> owner, String fieldName, String fieldDescriptor, boolean isStatic) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        String ownerInternalName = Type.getInternalName(owner);

        // 定义类: public final, 继承抽象访问器基类
        cw.visit(V17, ACC_PUBLIC | ACC_FINAL, className, null, ABSTRACT_CLASS_INTERNAL_NAME, null);

        // 生成默认构造器, 仅转发调用 super()
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, ABSTRACT_CLASS_INTERNAL_NAME, "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        // 生成 get 方法: 读取字段并装箱为 Object 返回. 静态字段用 GETSTATIC, 实例字段先转型接收者再 GETFIELD
        mv = cw.visitMethod(ACC_PUBLIC, "get", "(Ljava/lang/Object;)Ljava/lang/Object;", null, null);
        mv.visitCode();
        if (isStatic) {
            mv.visitFieldInsn(GETSTATIC, ownerInternalName, fieldName, fieldDescriptor);
        } else {
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, ownerInternalName);
            mv.visitFieldInsn(GETFIELD, ownerInternalName, fieldName, fieldDescriptor);
        }
        ASMUtils.box(mv, fieldDescriptor);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        // 生成 set 方法: 将 Object 入参拆箱/转型后写入字段. 静态字段用 PUTSTATIC, 实例字段先转型接收者再 PUTFIELD
        mv = cw.visitMethod(ACC_PUBLIC, "set", "(Ljava/lang/Object;Ljava/lang/Object;)V", null, null);
        mv.visitCode();
        if (isStatic) {
            mv.visitVarInsn(ALOAD, 2);
        } else {
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, ownerInternalName);
            mv.visitVarInsn(ALOAD, 2);
        }
        ASMUtils.unboxAndCast(mv, fieldDescriptor);
        mv.visitFieldInsn(isStatic ? PUTSTATIC : PUTFIELD, ownerInternalName, fieldName, fieldDescriptor);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }
}