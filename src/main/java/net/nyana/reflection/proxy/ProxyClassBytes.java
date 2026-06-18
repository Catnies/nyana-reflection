package net.nyana.reflection.proxy;

import java.lang.invoke.MethodHandle;
import java.util.List;

/**
 * ASM 写入阶段的产物, 包含类字节码和需要回填到静态字段的 MethodHandle
 */
record ProxyClassBytes(
        byte[] bytecode,
        List<MethodHandle> staticHandleBindings
) {
}
