package net.nyana.reflection.condition;

import org.jetbrains.annotations.NotNull;

public abstract class CustomCondition {

    public abstract boolean check(@NotNull String[] value);
}
