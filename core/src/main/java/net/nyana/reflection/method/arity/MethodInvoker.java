package net.nyana.reflection.method.arity;

import org.jetbrains.annotations.Nullable;

public abstract class MethodInvoker {

    public abstract Object invoke(@Nullable Object instance, Object... args);
}
