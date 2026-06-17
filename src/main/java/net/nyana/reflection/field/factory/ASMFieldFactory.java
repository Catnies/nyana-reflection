package net.nyana.reflection.field.factory;

import net.nyana.reflection.exception.ReflectionException;
import net.nyana.reflection.field.ASMFieldAccessor;
import net.nyana.reflection.field.primitive.*;

import java.lang.reflect.Field;

// ASM 字段访问器工厂接口: 由持有 Field 的类型实现, asm() 生成通用访问器, asm$xxx() 生成对应基本类型的专用访问器.
public interface ASMFieldFactory {

    Field field();

    // 生成以 Object 读写字段的通用访问器(字段值自动装箱/拆箱).
    default ASMFieldAccessor asm() {
        try {
            return FieldAccessorFactory.create(this.field());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create ASM field accessor", e);
        }
    }

    default SIntField asm$int() {
        try {
            return IntFieldAccessorFactory.create(this.field());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create ASM int accessor", e);
        }
    }

    default SFloatField asm$float() {
        try {
            return FloatFieldAccessorFactory.create(this.field());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create ASM float accessor", e);
        }
    }

    default SDoubleField asm$double() {
        try {
            return DoubleFieldAccessorFactory.create(this.field());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create ASM double accessor", e);
        }
    }

    default SBooleanField asm$boolean() {
        try {
            return BooleanFieldAccessorFactory.create(this.field());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create ASM boolean accessor", e);
        }
    }

    default SByteField asm$byte() {
        try {
            return ByteFieldAccessorFactory.create(this.field());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create ASM byte accessor", e);
        }
    }

    default SShortField asm$short() {
        try {
            return ShortFieldAccessorFactory.create(this.field());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create ASM short accessor", e);
        }
    }

    default SCharField asm$char() {
        try {
            return CharFieldAccessorFactory.create(this.field());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create ASM char accessor", e);
        }
    }

    default SLongField asm$long() {
        try {
            return LongFieldAccessorFactory.create(this.field());
        } catch (Throwable e) {
            throw new ReflectionException("Failed to create ASM long accessor", e);
        }
    }
}
