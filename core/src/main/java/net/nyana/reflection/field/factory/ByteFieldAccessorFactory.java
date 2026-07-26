package net.nyana.reflection.field.factory;

import net.nyana.reflection.NyanaReflection;
import net.nyana.reflection.field.primitive.SByteField;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

// byte 字段的 ASM 访问器工厂. 为目标字段生成隐藏类, 直接以 byte 读写, 免去装箱/拆箱开销.
@SuppressWarnings("DuplicatedCode")
final class ByteFieldAccessorFactory implements Opcodes {
    private ByteFieldAccessorFactory() {}
    private static final String ABSTRACT_CLASS_INTERNAL_NAME = Type.getInternalName(SByteField.class); // 生成类继承的抽象基类内部名

    // 校验字段类型后生成字节码, 定义为宿主类的嵌套隐藏类并实例化返回.
    static SByteField create(Field field) throws Exception {
        // 仅接受 byte 字段, 类型不符直接拒绝
        if (field.getType() != byte.class) {
            throw new IllegalArgumentException("Field must be of type byte");
        }
        Class<?> owner = field.getDeclaringClass();
        String fieldName = field.getName();
        boolean isStatic = Modifier.isStatic(field.getModifiers());
        String internalClassName = Type.getInternalName(owner) + "$" + NyanaReflection.getAsmClassPrefix() + "Field_" + fieldName;
        byte[] bytes = generateByteCode(internalClassName, owner, fieldName, isStatic);
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(owner, NyanaReflection.getLookup());
        MethodHandles.Lookup hiddenLookup = lookup.defineHiddenClass(bytes, true, MethodHandles.Lookup.ClassOption.NESTMATE);
        return (SByteField) hiddenLookup.lookupClass().getDeclaredConstructor().newInstance();
    }

    // 生成访问器类字节码: 无参构造器 + 基本类型版 get/set 方法, 直接读写字段无需装箱.
    private static byte[] generateByteCode(String className, Class<?> owner, String fieldName, boolean isStatic) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        String ownerInternalName = Type.getInternalName(owner);

        // 定义类并生成默认构造器(转发 super), 随后生成 get/set
        cw.visit(V17, ACC_PUBLIC | ACC_FINAL, className, null, ABSTRACT_CLASS_INTERNAL_NAME, null);

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, ABSTRACT_CLASS_INTERNAL_NAME, "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC, "get", "(Ljava/lang/Object;)B", null, null);
        mv.visitCode();
        if (isStatic) {
            mv.visitFieldInsn(GETSTATIC, ownerInternalName, fieldName, "B");
        } else {
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, ownerInternalName);
            mv.visitFieldInsn(GETFIELD, ownerInternalName, fieldName, "B");
        }
        mv.visitInsn(IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC, "set", "(Ljava/lang/Object;B)V", null, null);
        mv.visitCode();
        if (isStatic) {
            mv.visitVarInsn(ILOAD, 2);
            mv.visitFieldInsn(PUTSTATIC, ownerInternalName, fieldName, "B");
        } else {
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, ownerInternalName);
            mv.visitVarInsn(ILOAD, 2);
            mv.visitFieldInsn(PUTFIELD, ownerInternalName, fieldName, "B");
        }
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }
}