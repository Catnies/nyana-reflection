package net.nyana.reflection.constructor;

import net.nyana.reflection.NyanaReflection;
import net.nyana.reflection.constructor.factory.ASMConstructorFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.util.Objects;

// 对 Constructor 的封装, 提供 MethodHandle 解析, 并经 ASMConstructorFactory 暴露 ASM 构造器调用器入口.
public final class NyanaConstructor<T> implements ASMConstructorFactory {
    public final Constructor<T> constructor;

    public NyanaConstructor(Constructor<T> constructor) {
        this.constructor = constructor;
    }

    @NotNull
    public static <T> NyanaConstructor<T> of(@NotNull final Constructor<T> constructor) {
        Objects.requireNonNull(constructor, "constructor cannot be null");
        return new NyanaConstructor<>(constructor);
    }

    @Nullable
    public static <T> NyanaConstructor<T> ofNullable(@Nullable final Constructor<T> constructor) {
        return constructor == null ? null : new NyanaConstructor<T>(constructor);
    }

    public MethodHandle unreflect() {
        return NyanaReflection.unreflectConstructor(this.constructor);
    }

    @Override
    public Constructor<T> constructor() {
        return this.constructor;
    }
}
