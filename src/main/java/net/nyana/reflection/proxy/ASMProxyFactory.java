package net.nyana.reflection.proxy;

public final class ASMProxyFactory {
    private ASMProxyFactory() {
    }

    public static <T> T create(final Class<T> proxy) {
        return Util.createAsmProxy(proxy);
    }
}
