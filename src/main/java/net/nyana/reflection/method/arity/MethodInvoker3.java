package net.nyana.reflection.method.arity;

import org.jetbrains.annotations.Nullable;

public abstract class MethodInvoker3 {

    public abstract Object invoke(@Nullable Object instance,
                  @Nullable Object arg0,
                  @Nullable Object arg1,
                  @Nullable Object arg2);
}
