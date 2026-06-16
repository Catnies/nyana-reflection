package net.nyana.reflection;

import net.nyana.reflection.exception.ReflectionException;
import net.nyana.reflection.remapper.Remapper;
import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Predicate;

public final class NyanaReflection {
    private static volatile Unsafe UNSAFE;
    private static volatile MethodHandles.Lookup LOOKUP;
    private static volatile boolean INITIALIZED;
    private static MethodHandle constructor$MemberName;
    private static MethodHandle method$MemberName$getReferenceKind;
    private static MethodHandle method$MethodHandles$Lookup$getDirectField;

    private static String PREFIX = "Nyana";
    private static Remapper REMAPPER = Remapper.noOp();
    private static Predicate<String> ACTIVE_PREDICATE = s -> true;

    private NyanaReflection() {}

    // 双IF单例, 不允许多次初始化.
    public static void init(MethodHandles.Lookup lookup) {
        if (!INITIALIZED) {
            synchronized (NyanaReflection.class) {
                if (!INITIALIZED) {
                    try {
                        UNSAFE = (Unsafe) setAccessible(Unsafe.class.getDeclaredField("theUnsafe")).get(null);
                        LOOKUP = lookup == null ? getLookup(UNSAFE) : lookup;

                        Class<?> clazz$MemberName = Class.forName("java.lang.invoke.MemberName");
                        constructor$MemberName = LOOKUP.unreflectConstructor(clazz$MemberName.getDeclaredConstructor(Field.class, boolean.class));
                        method$MemberName$getReferenceKind = LOOKUP.unreflect(clazz$MemberName.getDeclaredMethod("getReferenceKind"));
                        method$MethodHandles$Lookup$getDirectField = LOOKUP.unreflect(MethodHandles.Lookup.class.getDeclaredMethod("getDirectField", byte.class, Class.class, clazz$MemberName));
                        INITIALIZED = true;
                    } catch (Throwable e) {
                        throw new ReflectionException("Failed to init Reflection", e);
                    }
                }
            }
        }
    }

    // 获取神权 Lookup
    private static MethodHandles.Lookup getLookup(Unsafe unsafe) throws NoSuchFieldException {
        Field implLookup = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
        long offset = unsafe.staticFieldOffset(implLookup);
        return (MethodHandles.Lookup) unsafe.getObject(MethodHandles.Lookup.class, offset);
    }

    // 分配对象内存
    @SuppressWarnings("unchecked")
    public static <T> T allocateInstance(Class<T> clazz) {
        try {
            return (T) getUnsafe().allocateInstance(clazz);
        } catch (InstantiationException e) {
            return null;
        }
    }

    // 打开字段访问权限
    @NotNull
    public static <T extends AccessibleObject> T setAccessible(@NotNull final T o) {
        o.setAccessible(true);
        return o;
    }

    // 获取 Field 对应的 VarHandle
    public static VarHandle unreflectVarHandle(@NotNull final Field field) {
        try {
            return getLookup().unreflectVarHandle(field);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    // 获取 Field 对应的 Getter MethodHandle
    public static MethodHandle unreflectGetter(@NotNull final Field field) {
        try {
            return getLookup().unreflectGetter(field);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    // 获取 Field 对应的 Setter MethodHandle.
    public static MethodHandle unreflectSetter(@NotNull final Field field) {
        try {
            return getLookup().unreflectSetter(field);
        } catch (IllegalAccessException e) {
            try {
                /*
                 * 绕过 final限制 获取方法句柄.
                 *
                 * 如果遇到 static final 修饰的字段, 神权 MethodHandle.LookUp 是无法修改的.
                 * 参考 MethodHandles#unreflectField 方法里的实现, 除了对LookUp鉴权,还要对 Field 生产出的 MemberName 检查.
                 * fallback 方案本质上是伪造了 MemberName , 然后直接调用最终的生产方法返回 MethodHandle.
                 */
                Object memberName = constructor$MemberName.invoke(field, /*makeSetter*/ true);
                Object refKind = method$MemberName$getReferenceKind.invoke(memberName);
                return (MethodHandle) method$MethodHandles$Lookup$getDirectField.invoke(LOOKUP, refKind, field.getDeclaringClass(), memberName);
            } catch (Throwable ex) {
                return null;
            }
        }
    }

    // 获取 Constructor 对应的 MethodHandle
    public static MethodHandle unreflectConstructor(@NotNull final Constructor<?> constructor) {
        try {
            return getLookup().unreflectConstructor(constructor);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    // 获取 Method 对应的 MethodHandle
    public static MethodHandle unreflectMethod(@NotNull final Method method) {
        try {
            return getLookup().unreflect(method);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    public static Unsafe getUnsafe() {
        if (UNSAFE == null) init(null);
        return UNSAFE;
    }

    public static MethodHandles.Lookup getLookup() {
        if (LOOKUP == null) init(null);
        return LOOKUP;
    }

    @NotNull
    public static String getAsmClassPrefix() {
        return NyanaReflection.PREFIX;
    }

    public static void setAsmClassPrefix(@NotNull String prefix) {
        NyanaReflection.PREFIX = Objects.requireNonNull(prefix);
    }

    @NotNull
    public static Remapper getRemapper() {
        return NyanaReflection.REMAPPER;
    }

    public static void setRemapper(@NotNull Remapper remapper) {
        NyanaReflection.REMAPPER = Objects.requireNonNull(remapper);
    }

    @NotNull
    public static Predicate<String> getActivePredicate() {
        return NyanaReflection.ACTIVE_PREDICATE;
    }

    public static void setActivePredicate(@NotNull Predicate<String> predicate) {
        NyanaReflection.ACTIVE_PREDICATE = Objects.requireNonNull(predicate);
    }
}
