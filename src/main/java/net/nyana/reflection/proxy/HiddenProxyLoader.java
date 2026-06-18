package net.nyana.reflection.proxy;

import net.nyana.reflection.NyanaReflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.List;

/**
 * 加载 ASM 生成的隐藏类, 并回填 final 字段 setter 所需的 MethodHandle
 */
final class HiddenProxyLoader {

    /**
     * 使用目标类的 private Lookup 定义 NESTMATE 隐藏类, 使代理类可直接访问目标私有成员
     */
    @SuppressWarnings("unchecked")
    <T> T load(ProxyDefinition definition, ProxyClassBytes bytes) {
        try {
            // 借助目标类的 Lookup 定义隐藏类, NESTMATE 是访问私有字段/方法的关键约束
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(definition.targetType(), NyanaReflection.getLookup());
            MethodHandles.Lookup hiddenLookup = lookup.defineHiddenClass(
                    bytes.bytecode(),
                    true,
                    MethodHandles.Lookup.ClassOption.NESTMATE
            );
            Class<?> proxyClass = hiddenLookup.lookupClass();

            // final 字段无法直接 PUTFIELD, 生成类通过静态 MethodHandle 槽位间接调用 setter
            injectStaticHandles(proxyClass, bytes.staticHandleBindings());
            return (T) proxyClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create proxy class " + definition.proxyType(), e);
        }
    }

    private static void injectStaticHandles(
            Class<?> proxyClass,
            List<MethodHandle> staticHandleBindings
    ) throws ReflectiveOperationException {
        for (int i = 0; i < staticHandleBindings.size(); i++) {
            Field handleField = proxyClass.getDeclaredField("HANDLE_" + i);
            NyanaReflection.setAccessible(handleField).set(null, staticHandleBindings.get(i));
        }
    }
}
