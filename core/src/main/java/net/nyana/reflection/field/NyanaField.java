package net.nyana.reflection.field;

import net.nyana.reflection.NyanaReflection;
import net.nyana.reflection.field.factory.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.Objects;

// 对 Field 的封装, 提供 MethodHandle/VarHandle 解析, 并经 ASMFieldFactory 暴露 ASM 字段访问器入口.
public final class NyanaField implements ASMFieldFactory {
    public final Field field;

    public NyanaField(Field field) {
        this.field = field;
    }

    public static NyanaField of(@NotNull final Field field) {
        Objects.requireNonNull(field, "field cannot be null");
        return new NyanaField(field);
    }

    public static NyanaField ofNullable(@Nullable final Field field) {
        return field == null ? null : new NyanaField(field);
    }

    public MethodHandle unreflectGetter() {
        return NyanaReflection.unreflectGetter(this.field);
    }

    public MethodHandle unreflectSetter() {
        return NyanaReflection.unreflectSetter(this.field);
    }

    public VarHandle unreflectVarHandle() {
        return NyanaReflection.unreflectVarHandle(this.field);
    }

    // 基于 MethodHandle 的字段访问器, 作为 ASM 访问器之外的回退方案.
    public ASMFieldAccessor mh() {
        return new MethodHandleASMFieldAccessor(this.field);
    }

    @Override
    public Field field() {
        return field;
    }
}
