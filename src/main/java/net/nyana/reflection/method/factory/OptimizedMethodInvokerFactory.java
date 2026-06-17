package net.nyana.reflection.method.factory;

import net.nyana.reflection.NyanaReflection;
import net.nyana.reflection.method.arity.*;
import net.nyana.reflection.util.ASMUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

// 基于 ASM 生成定长参数方法调用器的工厂. 按参数个数选用 MethodInvoker0..10 抽象基类, 生成省去 Object[] 打包的优化调用器.
@SuppressWarnings("DuplicatedCode")
final class OptimizedMethodInvokerFactory implements Opcodes {
    private OptimizedMethodInvokerFactory() {}
    private static final Class<?>[] INTERFACES = new Class<?>[]{ // 下标即参数个数, 对应不同 arity 的抽象调用器
            MethodInvoker0.class, MethodInvoker1.class, MethodInvoker2.class, MethodInvoker3.class,
            MethodInvoker4.class, MethodInvoker5.class, MethodInvoker6.class, MethodInvoker7.class,
            MethodInvoker8.class, MethodInvoker9.class, MethodInvoker10.class
    };

    // 读取方法元信息, 按参数个数挑选抽象基类, 生成并实例化定长入参的调用器.
    @SuppressWarnings("unchecked")
    static <T> T create(Method method) throws Exception {
        Class<?> owner = method.getDeclaringClass();
        String methodName = method.getName();
        String methodDescriptor = Type.getMethodDescriptor(method);
        boolean isStatic = Modifier.isStatic(method.getModifiers());
        Class<?>[] parameterTypes = method.getParameterTypes();
        Class<?> returnType = method.getReturnType();
        Class<?> targetInterface = INTERFACES[parameterTypes.length]; // 按形参个数选取对应 arity 的抽象基类
        String internalClassName = Type.getInternalName(owner) + "$" + NyanaReflection.getAsmClassPrefix() + "Method_" + methodName;

        byte[] bytes = generateByteCode(
                internalClassName,
                owner,
                methodName,
                methodDescriptor,
                isStatic,
                parameterTypes,
                returnType,
                targetInterface
        );

        // 借助宿主类的私有 Lookup 定义隐藏类, NESTMATE 使其可访问宿主的私有成员
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(owner, NyanaReflection.getLookup());
        MethodHandles.Lookup hiddenLookup = lookup.defineHiddenClass(bytes, true, MethodHandles.Lookup.ClassOption.NESTMATE);
        return (T) hiddenLookup.lookupClass().getDeclaredConstructor().newInstance();
    }

    private static byte[] generateByteCode(
            String className,
            Class<?> owner,
            String methodName,
            String methodDescriptor,
            boolean isStatic,
            Class<?>[] params,
            Class<?> returnType,
            Class<?> abstractClass) {

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

        // invoke 描述符: 首个 Object 为实例, 其后每个形参各一个 Object, 返回 Object
        String desc = "(Ljava/lang/Object;" + "Ljava/lang/Object;".repeat(params.length) +
                ")Ljava/lang/Object;";

        mv = cw.visitMethod(ACC_PUBLIC, "invoke", desc, null, null);
        mv.visitCode();

        // 实例方法需加载并转型 this 接收者(槽位 1), 静态方法跳过
        if (!isStatic) {
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(owner));
        }

        // 逐个加载形参槽位(实例占槽 1, 故从 i+2 起), 拆箱/转型为方法形参类型
        for (int i = 0; i < params.length; i++) {
            mv.visitVarInsn(ALOAD, i + 2);
            ASMUtils.unboxAndCast(mv, Type.getDescriptor(params[i]));
        }

        // 按方法归属选择调用指令: 静态/接口/虚方法
        int opcode = isStatic ? INVOKESTATIC : (owner.isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL);
        mv.visitMethodInsn(opcode, Type.getInternalName(owner), methodName, methodDescriptor, owner.isInterface());

        // 统一返回 Object: void 方法压入 null, 其余把返回值装箱
        if (returnType == void.class) {
            mv.visitInsn(ACONST_NULL);
        } else {
            ASMUtils.box(mv, Type.getDescriptor(returnType));
        }

        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }
}