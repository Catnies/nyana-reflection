package net.nyana.reflection.constructor.arity;

import net.nyana.reflection.NyanaReflection;
import net.nyana.reflection.exception.ReflectionException;

// 绕过所有构造器, 借助 Unsafe 直接分配对象内存来创建实例(不执行任何构造逻辑与字段初始化).
public final class UnsafeConstructorInvoker {
    private final Class<?> clazz;

    public UnsafeConstructorInvoker(Class<?> clazz) {
        this.clazz = clazz;
    }

    // 直接分配实例, 跳过构造器; 失败时包装为 ReflectionException.
    public Object newInstance() {
        try {
            return NyanaReflection.getUnsafe().allocateInstance(this.clazz);
        } catch (InstantiationException e) {
            throw new ReflectionException("Failed to create " + this.clazz.getName() + " instance with unsafe methods", e);
        }
    }
}
