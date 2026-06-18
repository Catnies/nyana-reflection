package net.nyana.reflection.proxy;

/**
 * 基于 ASM 为带有 proxy 注解的接口创建运行时代理实例
 */
public final class ASMProxyFactory {
    private ASMProxyFactory() {
    }

    /**
     * 创建代理接口的实现实例, 接口本身需要使用 @ReflectionProxy 声明目标类型
     */
    public static <T> T create(final Class<T> proxy) {
        return ProxyGenerator.create(proxy);
    }
}
