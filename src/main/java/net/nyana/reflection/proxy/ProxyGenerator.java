package net.nyana.reflection.proxy;

/**
 * proxy 包的内部生成入口, 负责把公开的代理接口转换成可实例化的 ASM 隐藏类
 */
final class ProxyGenerator {
    private static final ProxyBinder BINDER = new ProxyBinder(); // 注解解析与成员绑定
    private static final ProxyClassWriter WRITER = new ProxyClassWriter(); // ASM 字节码写入
    private static final HiddenProxyLoader LOADER = new HiddenProxyLoader(); // 隐藏类加载与实例化

    private ProxyGenerator() {}

    /**
     * 按 bind -> write -> load 三个阶段创建代理实例
     */
    static <T> T create(Class<T> proxyType) {
        // 解析 proxy 类, 然后将解析的代理方法对象和目标类的成员进行绑定
        ProxyDefinition definition = BINDER.bind(proxyType);
        if (definition == null) {
            return null;
        }
        // 根据 ProxyDefinition 定义, 用 ASM 生成一个目标类的内部代理类实现.
        ProxyClassBytes bytes = WRITER.write(definition);
        // 使用加载器加载生成的类.
        return LOADER.load(definition, bytes);
    }
}
