package net.nyana.reflection.method;

import net.nyana.reflection.NyanaReflection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Objects;

// 对 Method 的封装, 提供 MethodHandle 解析入口.
public final class NyanaMethod {
    public final Method method;

    public NyanaMethod(Method method) {
        this.method = method;
    }

    public static NyanaMethod of(@NotNull final Method method) {
        Objects.requireNonNull(method, "method cannot be null");
        return new NyanaMethod(method);
    }

    public static NyanaMethod ofNullable(@Nullable final Method method) {
        return method == null ? null : new NyanaMethod(method);
    }

    public MethodHandle unreflect() {
        return NyanaReflection.unreflectMethod(this.method);
    }


}
