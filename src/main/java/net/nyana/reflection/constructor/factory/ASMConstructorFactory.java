package net.nyana.reflection.constructor.factory;

import net.nyana.reflection.constructor.arity.*;
import net.nyana.reflection.exception.ReflectionException;

import java.lang.reflect.Constructor;

// ASM 构造器调用器工厂接口: 由持有 Constructor 的类型实现, asm() 生成变长入参调用器, asm$N() 生成对应参数个数的定长调用器.
public interface ASMConstructorFactory {

    Constructor<?> constructor();

    // 生成以 Object[] 变长入参调用构造器的通用调用器.
    default ConstructorInvoker asm() {
        try {
            return ConstructorInvokerFactory.create(this.constructor());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create Constructor invoker", e);
        }
    }

    // asm$0()..asm$10() 按固定参数个数生成定长调用器, 省去 Object[] 打包(此处为无参版本).
    default ConstructorInvoker0 asm$0() {
        try {
            return OptimizedConstructorInvokerFactory.create(this.constructor());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create Constructor0 invoker", e);
        }
    }

    default ConstructorInvoker1 asm$1() {
        try {
            return OptimizedConstructorInvokerFactory.create(this.constructor());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create Constructor1 invoker", e);
        }
    }

    default ConstructorInvoker2 asm$2() {
        try {
            return OptimizedConstructorInvokerFactory.create(this.constructor());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create Constructor2 invoker", e);
        }
    }

    default ConstructorInvoker3 asm$3() {
        try {
            return OptimizedConstructorInvokerFactory.create(this.constructor());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create Constructor3 invoker", e);
        }
    }

    default ConstructorInvoker4 asm$4() {
        try {
            return OptimizedConstructorInvokerFactory.create(this.constructor());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create Constructor4 invoker", e);
        }
    }

    default ConstructorInvoker5 asm$5() {
        try {
            return OptimizedConstructorInvokerFactory.create(this.constructor());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create Constructor5 invoker", e);
        }
    }

    default ConstructorInvoker6 asm$6() {
        try {
            return OptimizedConstructorInvokerFactory.create(this.constructor());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create Constructor6 invoker", e);
        }
    }

    default ConstructorInvoker7 asm$7() {
        try {
            return OptimizedConstructorInvokerFactory.create(this.constructor());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create Constructor7 invoker", e);
        }
    }

    default ConstructorInvoker8 asm$8() {
        try {
            return OptimizedConstructorInvokerFactory.create(this.constructor());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create Constructor8 invoker", e);
        }
    }

    default ConstructorInvoker9 asm$9() {
        try {
            return OptimizedConstructorInvokerFactory.create(this.constructor());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create Constructor9 invoker", e);
        }
    }

    default ConstructorInvoker10 asm$10() {
        try {
            return OptimizedConstructorInvokerFactory.create(this.constructor());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create Constructor10 invoker", e);
        }
    }
}
