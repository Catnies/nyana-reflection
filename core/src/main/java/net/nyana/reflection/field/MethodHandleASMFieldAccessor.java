package net.nyana.reflection.field;

import net.nyana.reflection.NyanaReflection;
import net.nyana.reflection.exception.ReflectionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;

// 基于 MethodHandle 的字段访问器实现, 作为 ASM 访问器的回退方案. 构造时预先把句柄适配成原始类型版与全 Object 版两套, 读写时按需选用.
final class MethodHandleASMFieldAccessor extends ASMFieldAccessor {
    private final boolean isStatic; // 静态字段无接收者, 句柄签名与实例字段不同
    private final MethodHandle rawGetter; // 原始类型 getter, 返回字段真实类型
    private final MethodHandle rawSetter; // 原始类型 setter, 接收字段真实类型
    private final MethodHandle genericGetter; // 全 Object 签名 getter, 供 get(Object) 使用
    private final MethodHandle genericSetter; // 全 Object 签名 setter, 供 set(Object, Object) 使用

    MethodHandleASMFieldAccessor(@NotNull Field field) {
        this.isStatic = Modifier.isStatic(field.getModifiers());

        // 解除字段访问限制, 取得原始的 getter/setter 方法句柄
        MethodHandle getter = Objects.requireNonNull(NyanaReflection.unreflectGetter(field));
        MethodHandle setter = Objects.requireNonNull(NyanaReflection.unreflectSetter(field));

        // 原始类型句柄: 实例字段把接收者参数泛化为 Object 以便统一传入, 静态字段无接收者保持原样
        if (this.isStatic) {
            this.rawGetter = getter;
            this.rawSetter = setter;
        } else {
            this.rawGetter = getter.asType(getter.type().changeParameterType(0, Object.class));
            this.rawSetter = setter.asType(setter.type().changeParameterType(0, Object.class));
        }

        // 通用句柄: 进一步把字段值类型也泛化为 Object, 供 get(Object)/set(Object, Object) 直接调用
        if (this.isStatic) {
            this.genericGetter = this.rawGetter.asType(MethodType.methodType(Object.class));
            this.genericSetter = this.rawSetter.asType(MethodType.methodType(void.class, Object.class));
        } else {
            this.genericGetter = this.rawGetter.asType(MethodType.methodType(Object.class, Object.class));
            this.genericSetter = this.rawSetter.asType(MethodType.methodType(void.class, Object.class, Object.class));
        }
    }

    @Override
    public int getInt(Object instance) {
        try {
            return this.isStatic ? (int) this.rawGetter.invokeExact() : (int) this.rawGetter.invokeExact(instance);
        } catch (Throwable e) {
            throw new ReflectionException(e);
        }
    }

    @Override
    public long getLong(Object instance) {
        try {
            return this.isStatic ? (long) this.rawGetter.invokeExact() : (long) this.rawGetter.invokeExact(instance);
        } catch (Throwable e) {
            throw new ReflectionException(e);
        }
    }

    @Override
    public double getDouble(Object instance) {
        try {
            return this.isStatic ? (double) this.rawGetter.invokeExact() : (double) this.rawGetter.invokeExact(instance);
        } catch (Throwable e) {
            throw new ReflectionException(e);
        }
    }

    @Override
    public boolean getBoolean(Object instance) {
        try {
            return this.isStatic ? (boolean) this.rawGetter.invokeExact() : (boolean) this.rawGetter.invokeExact(instance);
        } catch (Throwable e) {
            throw new ReflectionException(e);
        }
    }

    @Override
    public float getFloat(Object instance) {
        try {
            return this.isStatic ? (float) this.rawGetter.invokeExact() : (float) this.rawGetter.invokeExact(instance);
        } catch (Throwable e) {
            throw new ReflectionException(e);
        }
    }

    @Override
    public void setInt(Object instance, int value) {
        try {
            if (this.isStatic) {
                this.rawSetter.invokeExact(value);
            } else {
                this.rawSetter.invokeExact(instance, value);
            }
        } catch (Throwable e) {
            throw new ReflectionException(e);
        }
    }

    @Override
    public void setLong(Object instance, long value) {
        try {
            if (this.isStatic) {
                this.rawSetter.invokeExact(value);
            } else {
                this.rawSetter.invokeExact(instance, value);
            }
        } catch (Throwable e) {
            throw new ReflectionException(e);
        }
    }

    @Override
    public void setDouble(Object instance, double value) {
        try {
            if (this.isStatic) {
                this.rawSetter.invokeExact(value);
            } else {
                this.rawSetter.invokeExact(instance, value);
            }
        } catch (Throwable e) {
            throw new ReflectionException(e);
        }
    }

    @Override
    public void setFloat(Object instance, float value) {
        try {
            if (this.isStatic) {
                this.rawSetter.invokeExact(value);
            } else {
                this.rawSetter.invokeExact(instance, value);
            }
        } catch (Throwable e) {
            throw new ReflectionException(e);
        }
    }

    @Override
    public void setBoolean(Object instance, boolean value) {
        try {
            if (this.isStatic) {
                this.rawSetter.invokeExact(value);
            } else {
                this.rawSetter.invokeExact(instance, value);
            }
        } catch (Throwable e) {
            throw new ReflectionException(e);
        }
    }

    @Override
    @Nullable
    public Object get(Object instance) {
        try {
            return this.isStatic ? this.genericGetter.invokeExact() : this.genericGetter.invokeExact(instance);
        } catch (Throwable e) {
            throw new ReflectionException(e);
        }
    }

    @Override
    public void set(Object instance, @Nullable Object value) {
        try {
            if (this.isStatic) {
                this.genericSetter.invokeExact(value);
            } else {
                this.genericSetter.invokeExact(instance, value);
            }
        } catch (Throwable e) {
            throw new ReflectionException(e);
        }
    }

    public boolean isStatic() {
        return this.isStatic;
    }
}