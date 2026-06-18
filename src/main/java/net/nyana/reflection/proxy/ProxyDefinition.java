package net.nyana.reflection.proxy;

import java.util.List;

/**
 * 代理接口解析后的内部定义, 只描述代理方法和目标成员的绑定关系, 不包含 ASM 细节
 */
record ProxyDefinition(
        Class<?> proxyType,
        Class<?> targetType,
        List<ProxyBinding> bindings
) {
}
