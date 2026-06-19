package net.nyana.reflection.proxy;

import java.util.List;

// 代理接口解析后的内部定义.
record ProxyDefinition(
        Class<?> proxyType,
        Class<?> targetType,
        List<ProxyBinding> bindings
) {
}
