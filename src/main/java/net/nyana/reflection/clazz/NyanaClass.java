package net.nyana.reflection.clazz;

import net.nyana.reflection.NyanaReflection;
import net.nyana.reflection.constructor.NyanaConstructor;
import net.nyana.reflection.constructor.arity.UnsafeConstructorInvoker;
import net.nyana.reflection.constructor.matcher.ConstructorMatcher;
import net.nyana.reflection.field.NyanaField;
import net.nyana.reflection.field.matcher.FieldMatcher;
import net.nyana.reflection.method.NyanaMethod;
import net.nyana.reflection.method.matcher.MethodMatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Predicate;

public final class NyanaClass<T> {
    public final Class<T> clazz;

    public NyanaClass(Class<T> clazz) {
        this.clazz = clazz;
    }

    // 根据非空 Class 创建一个 NyanaClass 实例.
    @NotNull
    public static <T> NyanaClass<T> of(@NotNull Class<T> clazz) {
        Objects.requireNonNull(clazz, "class cannot be null");
        return new NyanaClass<>(clazz);
    }

    // 根据可空 Class 创建一个 NyanaClass 实例, 如果为空则也返回空.
    @Nullable
    public static <T> NyanaClass<T> ofNullable(@Nullable Class<T> clazz) {
        return clazz == null ? null : new NyanaClass<>(clazz);
    }

    // 检查目标实例是否是当前 Class 的实例.
    public boolean isInstance(@NotNull Object object) {
        return this.clazz.isInstance(object);
    }

    // 根据可能的全类名搜索 Class 对象.
    @Nullable
    public static Class<?> find(String... classes) {
        if (classes.length == 1) {
            return find(classes[0]);
        }
        for (String className : classes) {
            Class<?> clazz = find(className);
            if (clazz != null) {
                return clazz;
            }
        }
        return null;
    }

    public static Class<?> find(String clazz) {
        try {
            return Class.forName(NyanaReflection.getRemapper().remapClassName(clazz));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    // 根据可能的全类名查找 Class 对象, 并使用提供的类加载器加载它.
    public static Class<?> find(boolean load, ClassLoader classLoader, String... classes) {
        if (classes.length == 1) {
            return find(load, classLoader, classes[0]);
        }
        for (String className : classes) {
            Class<?> clazz = find(load, classLoader, className);
            if (clazz != null) {
                return clazz;
            }
        }
        return null;
    }

    public static Class<?> find(boolean load, @Nullable ClassLoader classLoader, String clazz) {
        try {
            return Class.forName(NyanaReflection.getRemapper().remapClassName(clazz), load, classLoader);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    // 根据可能的全类名, 不经过 Remap 查找对应的 Class 对象.
    public static Class<?> findNoRemap(String... classes) {
        if (classes.length == 1) {
            return findNoRemap(classes[0]);
        }
        for (String className : classes) {
            Class<?> clazz = findNoRemap(className);
            if (clazz != null) {
                return clazz;
            }
        }
        return null;
    }

    public static Class<?> findNoRemap(String clazz) {
        try {
            return Class.forName(clazz);
        } catch (Throwable e) {
            return null;
        }
    }

    // 根据全类名, 检查当前类是否存在.
    public static boolean exists(String clazz) {
        try {
            Class.forName(NyanaReflection.getRemapper().remapClassName(clazz));
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean existsNoRemap(String clazz) {
        try {
            Class.forName(clazz);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    // 使用 FieldMatcher 匹配获取 Class 内符合条件的 Field
    public Field getField(FieldMatcher matcher) {
        return findMember(this.clazz.getFields(), matcher::matches, 0, false, false);
    }

    public Field getField(FieldMatcher matcher, int index) {
        return findMember(this.clazz.getFields(), matcher::matches, index, false, false);
    }

    // 使用 FieldMatcher 匹配获取 Class 内符合条件的 Field
    public Field getDeclaredField(FieldMatcher matcher) {
        return findMember(this.clazz.getDeclaredFields(), matcher::matches, 0, false, true);
    }

    public Field getDeclaredField(FieldMatcher matcher, int index) {
        return findMember(this.clazz.getDeclaredFields(), matcher::matches, index, false, true);
    }

    // 使用 FieldMatcher 反向遍历匹配获取 Class 内符合条件的 Field
    public Field getFieldBackwards(FieldMatcher matcher, int index) {
        return findMember(this.clazz.getFields(), matcher::matches, index, true, false);
    }

    public Field getDeclaredFieldBackwards(FieldMatcher matcher, int index) {
        return findMember(this.clazz.getDeclaredFields(), matcher::matches, index, true, true);
    }

    // 使用 FieldMatcher 匹配获取 Class 内符合条件的 Field
    public NyanaField getSparrowField(FieldMatcher matcher) {
        return NyanaField.ofNullable(getField(matcher));
    }

    public NyanaField getSparrowField(FieldMatcher matcher, int index) {
        return NyanaField.ofNullable(getField(matcher, index));
    }

    // 使用 FieldMatcher 匹配获取 Class 内符合条件的 Field
    public NyanaField getDeclaredSparrowField(FieldMatcher matcher) {
        return NyanaField.ofNullable(getDeclaredField(matcher));
    }

    public NyanaField getDeclaredSparrowField(FieldMatcher matcher, int index) {
        return NyanaField.ofNullable(getDeclaredField(matcher, index));
    }

    // 使用 FieldMatcher 反向遍历匹配获取 Class 内符合条件的 Field
    public NyanaField getSparrowFieldBackwards(FieldMatcher matcher, int index) {
        return NyanaField.ofNullable(getFieldBackwards(matcher, index));
    }

    public NyanaField getDeclaredSparrowFieldBackwards(FieldMatcher matcher, int index) {
        return NyanaField.ofNullable(getDeclaredFieldBackwards(matcher, index));
    }

    // 绕过所有构造器, 直接分配对象内存进行构建对象
    public UnsafeConstructorInvoker unsafeConstructor() {
        return new UnsafeConstructorInvoker(this.clazz);
    }

    // 使用 ConstructorMatcher 匹配获取 Class 内符合条件的 Constructor
    @SuppressWarnings("unchecked")
    public Constructor<T> getConstructor(ConstructorMatcher matcher) {
        return (Constructor<T>) findMember(this.clazz.getConstructors(), matcher::matches, 0, false, false);
    }

    @SuppressWarnings("unchecked")
    public Constructor<T> getConstructor(ConstructorMatcher matcher, int index) {
        return (Constructor<T>) findMember(this.clazz.getConstructors(), matcher::matches, index, false, false);
    }

    // 使用 ConstructorMatcher 匹配获取 Class 内符合条件的 Constructor
    @SuppressWarnings("unchecked")
    public Constructor<T> getDeclaredConstructor(ConstructorMatcher matcher) {
        return (Constructor<T>) findMember(this.clazz.getDeclaredConstructors(), matcher::matches, 0, false, true);
    }

    @SuppressWarnings("unchecked")
    public Constructor<T> getDeclaredConstructor(ConstructorMatcher matcher, int index) {
        return (Constructor<T>) findMember(this.clazz.getDeclaredConstructors(), matcher::matches, index, false, true);
    }

    // 使用 ConstructorMatcher 反向遍历匹配获取 Class 内符合条件的 Constructor
    @SuppressWarnings("unchecked")
    public Constructor<T> getConstructorBackwards(ConstructorMatcher matcher, int index) {
        return (Constructor<T>) findMember(this.clazz.getConstructors(), matcher::matches, index, true, false);
    }

    @SuppressWarnings("unchecked")
    public Constructor<T> getDeclaredConstructorBackwards(ConstructorMatcher matcher, int index) {
        return (Constructor<T>) findMember(this.clazz.getDeclaredConstructors(), matcher::matches, index, true, true);
    }

    // 使用 ConstructorMatcher 匹配获取 Class 内符合条件的 Constructor
    public NyanaConstructor<T> getSparrowConstructor(ConstructorMatcher matcher) {
        return NyanaConstructor.ofNullable(getConstructor(matcher));
    }

    public NyanaConstructor<T> getSparrowConstructor(ConstructorMatcher matcher, int index) {
        return NyanaConstructor.ofNullable(getConstructor(matcher, index));
    }

    // 使用 ConstructorMatcher 匹配获取 Class 内符合条件的 Constructor
    public NyanaConstructor<T> getDeclaredSparrowConstructor(ConstructorMatcher matcher) {
        return NyanaConstructor.ofNullable(getDeclaredConstructor(matcher));
    }

    public NyanaConstructor<T> getDeclaredSparrowConstructor(ConstructorMatcher matcher, int index) {
        return NyanaConstructor.ofNullable(getDeclaredConstructor(matcher, index));
    }

    // 使用 ConstructorMatcher 反向遍历匹配获取 Class 内符合条件的 Constructor
    public NyanaConstructor<T> getSparrowConstructorBackwards(ConstructorMatcher matcher, int index) {
        return NyanaConstructor.ofNullable(getConstructorBackwards(matcher, index));
    }

    public NyanaConstructor<T> getDeclaredSparrowConstructorBackwards(ConstructorMatcher matcher, int index) {
        return NyanaConstructor.ofNullable(getDeclaredConstructorBackwards(matcher, index));
    }

    // 使用 MethodMatcher 匹配获取 Class 内符合条件的 Method
    public Method getMethod(MethodMatcher matcher) {
        return findMember(this.clazz.getMethods(), matcher::matches, 0, false, false);
    }

    public Method getMethod(MethodMatcher matcher, int index) {
        return findMember(this.clazz.getMethods(), matcher::matches, index, false, false);
    }

    // 使用 MethodMatcher 匹配获取 Class 内符合条件的 Method
    public Method getDeclaredMethod(MethodMatcher matcher) {
        return findMember(this.clazz.getDeclaredMethods(), matcher::matches, 0, false, true);
    }

    public Method getDeclaredMethod(MethodMatcher matcher, int index) {
        return findMember(this.clazz.getDeclaredMethods(), matcher::matches, index, false, true);
    }

    // 使用 MethodMatcher 反向遍历匹配获取 Class 内符合条件的 Method
    public Method getMethodBackwards(MethodMatcher matcher, int index) {
        return findMember(this.clazz.getMethods(), matcher::matches, index, true, false);
    }

    public Method getDeclaredMethodBackwards(MethodMatcher matcher, int index) {
        return findMember(this.clazz.getDeclaredMethods(), matcher::matches, index, true, true);
    }

    // 使用 MethodMatcher 匹配获取 Class 内符合条件的 Method
    public NyanaMethod getSparrowMethod(MethodMatcher matcher) {
        return NyanaMethod.ofNullable(getMethod(matcher));
    }

    public NyanaMethod getSparrowMethod(MethodMatcher matcher, int index) {
        return NyanaMethod.ofNullable(getMethod(matcher, index));
    }

    // 使用 MethodMatcher 匹配获取 Class 内符合条件的 Method
    public NyanaMethod getDeclaredSparrowMethod(MethodMatcher matcher) {
        return NyanaMethod.ofNullable(getDeclaredMethod(matcher));
    }

    public NyanaMethod getDeclaredSparrowMethod(MethodMatcher matcher, int index) {
        return NyanaMethod.ofNullable(getDeclaredMethod(matcher, index));
    }

    // 使用 MethodMatcher 反向遍历匹配获取 Class 内符合条件的 Method
    public NyanaMethod getSparrowMethodBackwards(MethodMatcher matcher, int index) {
        return NyanaMethod.ofNullable(getMethodBackwards(matcher, index));
    }

    public NyanaMethod getDeclaredSparrowMethodBackwards(MethodMatcher matcher, int index) {
        return NyanaMethod.ofNullable(getDeclaredMethodBackwards(matcher, index));
    }

    // 在成员数组中按匹配器查找第 index 个命中的成员. 支持正向/反向遍历, 并可选择对结果解除访问限制.
    private static <M extends AccessibleObject> M findMember(
            final M[] members,
            final Predicate<M> matcher,
            final int index,
            final boolean backwards,
            final boolean accessible
    ) {
        int matched = 0; // 已命中的成员个数
        final int length = members.length;
        for (int i = 0; i < length; i++) {
            // backwards 为 true 时从数组末尾向前取
            final M member = members[backwards ? length - 1 - i : i];
            if (matcher.test(member)) {
                // 命中计数到达目标下标即返回, 按需解除访问限制
                if (matched == index) {
                    return accessible ? NyanaReflection.setAccessible(member) : member;
                }
                matched++;
            }
        }
        return null;
    }
}
