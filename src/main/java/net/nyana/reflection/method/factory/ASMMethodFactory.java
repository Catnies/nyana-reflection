package net.nyana.reflection.method.factory;

import net.nyana.reflection.NyanaReflection;
import net.nyana.reflection.exception.ReflectionException;
import net.nyana.reflection.method.arity.MethodInvoker;
import net.nyana.reflection.method.arity.*;

import java.lang.reflect.Method;

// ASM 方法调用器工厂接口: 由持有 Method 的类型实现, asm() 生成变长入参调用器, asm$N() 生成对应参数个数的定长调用器.
public interface ASMMethodFactory {

    Method method();

    // 探测当前方法的 ASM 调用器是否可用; 返回 false 时 asm() 系列方法会抛出异常, 应改用 unreflect() 等基于 MethodHandle 的实现.
    default boolean isAsmSupported() {
        return NyanaReflection.isAsmSupported(this.method().getDeclaringClass());
    }

    // asm() 系列方法的前置检查, 类加载器不兼容时提前抛出带明确原因的异常.
    private void checkAsmSupported() {
        if (!this.isAsmSupported()) {
            throw new ReflectionException("ASM method invoker unavailable: '" + this.method().getDeclaringClass().getName() + "' is loaded by a class loader that cannot see nyana-reflection classes. Use unreflect() (MethodHandle-based) instead.");
        }
    }

    // 生成以 (实例, Object[]) 调用方法的通用调用器.
    default MethodInvoker asm() {
        this.checkAsmSupported();
        try {
            return MethodInvokerFactory.create(this.method());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create SMethod invoker", e);
        }
    }

    // asm$0()..asm$10() 按固定参数个数生成定长调用器, 省去 Object[] 打包(此处为无参版本).
    default MethodInvoker0 asm$0() {
        this.checkAsmSupported();
        try {
            return OptimizedMethodInvokerFactory.create(this.method());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create SMethod0 invoker", e);
        }
    }

    default MethodInvoker1 asm$1() {
        this.checkAsmSupported();
        try {
            return OptimizedMethodInvokerFactory.create(this.method());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create SMethod1 invoker", e);
        }
    }

    default MethodInvoker2 asm$2() {
        this.checkAsmSupported();
        try {
            return OptimizedMethodInvokerFactory.create(this.method());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create SMethod2 invoker", e);
        }
    }

    default MethodInvoker3 asm$3() {
        this.checkAsmSupported();
        try {
            return OptimizedMethodInvokerFactory.create(this.method());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create SMethod3 invoker", e);
        }
    }

    default MethodInvoker4 asm$4() {
        this.checkAsmSupported();
        try {
            return OptimizedMethodInvokerFactory.create(this.method());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create SMethod4 invoker", e);
        }
    }

    default MethodInvoker5 asm$5() {
        this.checkAsmSupported();
        try {
            return OptimizedMethodInvokerFactory.create(this.method());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create SMethod5 invoker", e);
        }
    }

    default MethodInvoker6 asm$6() {
        this.checkAsmSupported();
        try {
            return OptimizedMethodInvokerFactory.create(this.method());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create SMethod6 invoker", e);
        }
    }

    default MethodInvoker7 asm$7() {
        this.checkAsmSupported();
        try {
            return OptimizedMethodInvokerFactory.create(this.method());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create SMethod7 invoker", e);
        }
    }

    default MethodInvoker8 asm$8() {
        this.checkAsmSupported();
        try {
            return OptimizedMethodInvokerFactory.create(this.method());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create SMethod8 invoker", e);
        }
    }

    default MethodInvoker9 asm$9() {
        this.checkAsmSupported();
        try {
            return OptimizedMethodInvokerFactory.create(this.method());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create SMethod9 invoker", e);
        }
    }

    default MethodInvoker10 asm$10() {
        this.checkAsmSupported();
        try {
            return OptimizedMethodInvokerFactory.create(this.method());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create SMethod10 invoker", e);
        }
    }
}
