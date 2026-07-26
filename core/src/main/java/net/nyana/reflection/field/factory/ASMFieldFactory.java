package net.nyana.reflection.field.factory;

import net.nyana.reflection.NyanaReflection;
import net.nyana.reflection.exception.ReflectionException;
import net.nyana.reflection.field.ASMFieldAccessor;
import net.nyana.reflection.field.primitive.*;

import java.lang.reflect.Field;

// ASM 字段访问器工厂接口: 由持有 Field 的类型实现, asm() 生成通用访问器, asm$xxx() 生成对应基本类型的专用访问器.
public interface ASMFieldFactory {

    Field field();

    // 探测当前字段的 ASM 访问器是否可用; 返回 false 时 asm() 系列方法会抛出异常, 应改用 mh()/unreflect 系列等基于 MethodHandle 的实现.
    default boolean isAsmSupported() {
        return NyanaReflection.isAsmSupported(this.field().getDeclaringClass());
    }

    // asm() 系列方法的前置检查, 类加载器不兼容时提前抛出带明确原因的异常.
    private void checkAsmSupported() {
        if (!this.isAsmSupported()) {
            throw new ReflectionException("ASM field accessor unavailable: '" + this.field().getDeclaringClass().getName() + "' is loaded by a class loader that cannot see nyana-reflection classes. Use unreflectGetter()/unreflectSetter() or mh() (MethodHandle-based) instead.");
        }
    }

    // 生成以 Object 读写字段的通用访问器(字段值自动装箱/拆箱).
    default ASMFieldAccessor asm() {
        this.checkAsmSupported();
        try {
            return FieldAccessorFactory.create(this.field());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create ASM field accessor", e);
        }
    }

    default SIntField asm$int() {
        this.checkAsmSupported();
        try {
            return IntFieldAccessorFactory.create(this.field());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create ASM int accessor", e);
        }
    }

    default SFloatField asm$float() {
        this.checkAsmSupported();
        try {
            return FloatFieldAccessorFactory.create(this.field());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create ASM float accessor", e);
        }
    }

    default SDoubleField asm$double() {
        this.checkAsmSupported();
        try {
            return DoubleFieldAccessorFactory.create(this.field());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create ASM double accessor", e);
        }
    }

    default SBooleanField asm$boolean() {
        this.checkAsmSupported();
        try {
            return BooleanFieldAccessorFactory.create(this.field());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create ASM boolean accessor", e);
        }
    }

    default SByteField asm$byte() {
        this.checkAsmSupported();
        try {
            return ByteFieldAccessorFactory.create(this.field());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create ASM byte accessor", e);
        }
    }

    default SShortField asm$short() {
        this.checkAsmSupported();
        try {
            return ShortFieldAccessorFactory.create(this.field());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create ASM short accessor", e);
        }
    }

    default SCharField asm$char() {
        this.checkAsmSupported();
        try {
            return CharFieldAccessorFactory.create(this.field());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create ASM char accessor", e);
        }
    }

    default SLongField asm$long() {
        this.checkAsmSupported();
        try {
            return LongFieldAccessorFactory.create(this.field());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create ASM long accessor", e);
        }
    }
}
