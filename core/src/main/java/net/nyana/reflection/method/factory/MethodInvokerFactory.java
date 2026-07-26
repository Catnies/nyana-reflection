package net.nyana.reflection.method.factory;

import net.nyana.reflection.NyanaReflection;
import net.nyana.reflection.method.arity.MethodInvoker;
import net.nyana.reflection.util.ASMUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

// 基于 ASM 生成 MethodInvoker 的工厂. 为目标方法动态生成隐藏类, 以 (实例, Object[]) 形式调用方法, 规避反射开销.
@SuppressWarnings("DuplicatedCode")
final class MethodInvokerFactory implements Opcodes {
    private MethodInvokerFactory() {}
    private static final String SUPER_NAME = Type.getInternalName(MethodInvoker.class); // 生成类继承的抽象基类内部名

    // 读取方法元信息, 生成字节码并定义为宿主类的嵌套隐藏类, 实例化后返回调用器.
    static MethodInvoker create(Method method) throws Exception {
        Class<?> owner = method.getDeclaringClass();
        String methodName = method.getName();
        String methodDescriptor = Type.getMethodDescriptor(method);
        boolean isStatic = Modifier.isStatic(method.getModifiers());
        Class<?>[] parameterTypes = method.getParameterTypes();
        Class<?> returnType = method.getReturnType();

        String internalClassName = Type.getInternalName(owner) + "$" + NyanaReflection.getAsmClassPrefix() + "Method_" + methodName;

        byte[] bytes = generateByteCode(internalClassName, owner, methodName, methodDescriptor, isStatic, parameterTypes, returnType);

        // 借助宿主类的私有 Lookup 定义隐藏类, NESTMATE 使其可访问宿主的私有成员
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(owner, NyanaReflection.getLookup());
        MethodHandles.Lookup hiddenLookup = lookup.defineHiddenClass(bytes, true, MethodHandles.Lookup.ClassOption.NESTMATE);
        return (MethodInvoker) hiddenLookup.lookupClass().getDeclaredConstructor().newInstance();
    }

    // 生成调用器类字节码: 无参构造器 + invoke(实例, Object[]) 方法. 静态方法忽略实例参数.
    private static byte[] generateByteCode(String className, Class<?> owner, String methodName, String methodDescriptor, boolean isStatic, Class<?>[] params, Class<?> returnType) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        // 定义类: public final, 继承抽象调用器基类
        cw.visit(V17, ACC_PUBLIC | ACC_FINAL, className, null, SUPER_NAME, null);

        // 生成默认构造器, 仅转发调用 super()
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, SUPER_NAME, "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        // 生成 invoke 方法体: 槽位 1 为目标实例, 槽位 2 为 Object[] 实参数组
        mv = cw.visitMethod(ACC_PUBLIC, "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", null, null);
        mv.visitCode();

        // 实例方法需加载并转型 this 接收者, 静态方法跳过
        if (!isStatic) {
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(owner));
        }

        // 依次取出 Object[] 中的实参, 拆箱/转型为方法形参类型
        for (int i = 0; i < params.length; i++) {
            mv.visitVarInsn(ALOAD, 2);
            ASMUtils.pushInt(mv, i);
            mv.visitInsn(AALOAD);
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