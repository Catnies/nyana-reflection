package net.nyana.reflection.util;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

// ASM 字节码生成的公共工具, 负责基本类型的装箱/拆箱转型以及整型常量压栈.
public final class ASMUtils implements Opcodes {
    private ASMUtils() {}

    // 将栈顶的基本类型值装箱为对应包装类型. desc 是单字符类型描述符, 对象类型无需装箱直接保留.
    public static void box(MethodVisitor mv, String desc) {
        switch (desc) {
            case "Z": // boolean
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
                break;
            case "C": // char
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
                break;
            case "B": // byte
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
                break;
            case "S": // short
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
                break;
            case "I": // int
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                break;
            case "F": // float
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
                break;
            case "J": // long
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
                break;
            case "D": // double
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
                break;
            default:
                // 对象类型不需要装箱, 直接返回
                break;
        }
    }

    // 将栈顶的 Object 拆箱并转型为 desc 指定的类型. 用于把通用的 Object 入参还原成目标方法/字段的真实类型.
    public static void unboxAndCast(MethodVisitor mv, String desc) {
        switch (desc) {
            case "Z": // boolean
                mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
                break;
            case "C": // char
                mv.visitTypeInsn(CHECKCAST, "java/lang/Character");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false);
                break;
            case "B": // byte
                mv.visitTypeInsn(CHECKCAST, "java/lang/Byte");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B", false);
                break;
            case "S": // short
                mv.visitTypeInsn(CHECKCAST, "java/lang/Short");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S", false);
                break;
            case "I": // int
                mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
                break;
            case "F": // float
                mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false);
                break;
            case "J": // long
                mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
                break;
            case "D": // double
                mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false);
                break;
            case "V":
                // 如果 desc 是 void, 不需要转换, 直接返回即可
                return;
            default:
                // 对象类型
                if (desc.startsWith("L")) {
                    String internalName = desc.substring(1, desc.length() - 1);
                    mv.visitTypeInsn(CHECKCAST, internalName);
                }
                // 数组类型
                else if (desc.startsWith("[")) {
                    mv.visitTypeInsn(CHECKCAST, desc);
                } else {
                    throw new IllegalArgumentException("Unsupported field descriptor: " + desc);
                }
                break;
        }
    }

    // 向方法体压入一个 int 常量, 按数值大小选用最紧凑的指令(ICONST/BIPUSH). 这里仅用于压入参数下标, 取值远小于 Byte.MAX_VALUE.
    public static void pushInt(MethodVisitor mv, int value) {
        if (/* value >= -1 && */ value <= 5) {
            mv.visitInsn(ICONST_0 + value);
        } else if (/* value > 5 && */ value <= Byte.MAX_VALUE) {
            mv.visitIntInsn(BIPUSH, value);
        } else if (/* value > Byte.MAX_VALUE && */ value <= Short.MAX_VALUE) {
            mv.visitIntInsn(SIPUSH, value);
        } else {
            mv.visitLdcInsn(value);
        }
    }
}
