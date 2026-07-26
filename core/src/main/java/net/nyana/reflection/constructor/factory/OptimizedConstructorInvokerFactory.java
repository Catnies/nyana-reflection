package net.nyana.reflection.constructor.factory;

import net.nyana.reflection.NyanaReflection;
import net.nyana.reflection.constructor.arity.*;
import net.nyana.reflection.util.ASMUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;

// 基于 ASM 生成定长参数构造器调用器的工厂. 按参数个数选用 ConstructorInvoker0..10 抽象基类, 生成省去 Object[] 打包的优化调用器.
@SuppressWarnings("DuplicatedCode")
final class OptimizedConstructorInvokerFactory implements Opcodes {
    private OptimizedConstructorInvokerFactory() {}
    private static final Class<?>[] ABSTRACT_CLASSES = new Class<?>[]{ // 下标即参数个数, 对应不同 arity 的抽象调用器
            ConstructorInvoker0.class, ConstructorInvoker1.class, ConstructorInvoker2.class, ConstructorInvoker3.class,
            ConstructorInvoker4.class, ConstructorInvoker5.class, ConstructorInvoker6.class, ConstructorInvoker7.class,
            ConstructorInvoker8.class, ConstructorInvoker9.class, ConstructorInvoker10.class
    };

    // 读取构造器元信息, 按参数个数挑选抽象基类, 生成并实例化定长入参的调用器.
    @SuppressWarnings("unchecked")
    static <T> T create(Constructor<?> constructor) throws Exception {
        Class<?> owner = constructor.getDeclaringClass();
        String constructorDescriptor = Type.getConstructorDescriptor(constructor);
        Class<?>[] parameterTypes = constructor.getParameterTypes();

        // 按形参个数选取对应 arity 的抽象基类
        Class<?> targetAbstractClass = ABSTRACT_CLASSES[parameterTypes.length];
        String internalClassName = Type.getInternalName(owner) + "$" + NyanaReflection.getAsmClassPrefix() + "Constructor";

        byte[] bytes = generateByteCode(
                internalClassName,
                owner,
                constructorDescriptor,
                parameterTypes,
                targetAbstractClass
        );

        // 借助宿主类的私有 Lookup 定义隐藏类, NESTMATE 使其可访问宿主的私有成员
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(owner, NyanaReflection.getLookup());
        MethodHandles.Lookup hiddenLookup = lookup.defineHiddenClass(bytes, true, MethodHandles.Lookup.ClassOption.NESTMATE);
        return (T) hiddenLookup.lookupClass().getDeclaredConstructor().newInstance();
    }

    // 生成调用器类字节码: 无参构造器 + newInstance(Object,..,Object) 定长方法, 每个形参各占一个独立槽位.
    private static byte[] generateByteCode(
            String className,
            Class<?> owner,
            String desc,
            Class<?>[] params,
            Class<?> abstractClass
    ) {

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        String superName = Type.getInternalName(abstractClass);

        // 定义类: public final, 继承对应 arity 的抽象调用器
        cw.visit(V17, ACC_PUBLIC | ACC_FINAL, className, null, superName, null);

        // 生成默认构造器, 仅转发调用 super()
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, superName, "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        // 按参数个数拼出 newInstance 的方法描述符: 每个参数一个 Object, 返回 Object
        StringBuilder interfaceDesc = new StringBuilder("(");
        for (int i = 0; i < params.length; i++) {
            interfaceDesc.append("Ljava/lang/Object;");
        }
        interfaceDesc.append(")Ljava/lang/Object;");

        mv = cw.visitMethod(ACC_PUBLIC, "newInstance", interfaceDesc.toString(), null, null);
        mv.visitCode();

        // new 出目标对象并复制引用, 供构造器调用后返回
        String ownerInternalName = Type.getInternalName(owner);
        mv.visitTypeInsn(NEW, ownerInternalName);
        mv.visitInsn(DUP);

        // 逐个加载形参槽位(i+1), 拆箱/转型为构造器形参类型
        for (int i = 0; i < params.length; i++) {
            mv.visitVarInsn(ALOAD, i + 1);
            ASMUtils.unboxAndCast(mv, Type.getDescriptor(params[i]));
        }

        // 调用目标构造器并返回新建对象
        mv.visitMethodInsn(INVOKESPECIAL, ownerInternalName, "<init>", desc, false);

        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }
}