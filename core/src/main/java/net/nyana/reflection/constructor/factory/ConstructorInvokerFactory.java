package net.nyana.reflection.constructor.factory;

import net.nyana.reflection.NyanaReflection;
import net.nyana.reflection.constructor.arity.ConstructorInvoker;
import net.nyana.reflection.util.ASMUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;

// 基于 ASM 生成 ConstructorInvoker 的工厂. 为目标构造器动态生成隐藏类, 以 Object[] 变长入参形式调用构造器, 规避反射开销.
@SuppressWarnings("DuplicatedCode")
final class ConstructorInvokerFactory implements Opcodes {
    private ConstructorInvokerFactory() {}
    private static final String ABSTRACT_CLASS_INTERNAL_NAME = Type.getInternalName(ConstructorInvoker.class); // 生成类继承的抽象基类内部名

    // 读取构造器元信息, 生成字节码并定义为宿主类的嵌套隐藏类, 实例化后返回调用器.
    static ConstructorInvoker create(Constructor<?> constructor) throws Exception {
        Class<?> owner = constructor.getDeclaringClass();
        String constructorDescriptor = Type.getConstructorDescriptor(constructor);
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        String internalClassName = Type.getInternalName(owner) + "$" + NyanaReflection.getAsmClassPrefix() + "Constructor";
        byte[] bytes = generateByteCode(internalClassName, owner, constructorDescriptor, parameterTypes);

        // 借助宿主类的私有 Lookup 定义隐藏类, NESTMATE 使其可访问宿主的私有成员
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(owner, NyanaReflection.getLookup());
        MethodHandles.Lookup hiddenLookup = lookup.defineHiddenClass(bytes, true, MethodHandles.Lookup.ClassOption.NESTMATE);
        return (ConstructorInvoker) hiddenLookup.lookupClass().getDeclaredConstructor().newInstance();
    }

    // 生成调用器类的字节码: 一个无参构造器 + 一个 newInstance(Object[]) 方法.
    private static byte[] generateByteCode(String className, Class<?> owner, String descriptor, Class<?>[] params) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        // 定义类: public final, 继承抽象调用器基类
        cw.visit(V17, ACC_PUBLIC | ACC_FINAL, className, null, ABSTRACT_CLASS_INTERNAL_NAME, null);

        // 生成默认构造器, 仅转发调用 super()
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, ABSTRACT_CLASS_INTERNAL_NAME, "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        // 生成 newInstance 方法体, 入参为变长 Object[]
        mv = cw.visitMethod(ACC_PUBLIC | ACC_VARARGS, "newInstance", "([Ljava/lang/Object;)Ljava/lang/Object;", null, null);
        mv.visitCode();

        // new 出目标对象并复制引用, 供构造器调用后返回
        String ownerInternalName = Type.getInternalName(owner);
        mv.visitTypeInsn(NEW, ownerInternalName);
        mv.visitInsn(DUP);

        // 依次取出 Object[] 中的实参, 拆箱/转型为构造器形参类型
        for (int i = 0; i < params.length; i++) {
            mv.visitVarInsn(ALOAD, 1);
            ASMUtils.pushInt(mv, i);
            mv.visitInsn(AALOAD);
            ASMUtils.unboxAndCast(mv, Type.getDescriptor(params[i]));
        }

        // 调用目标构造器并返回新建对象
        mv.visitMethodInsn(INVOKESPECIAL, ownerInternalName, "<init>", descriptor, false);

        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }
}