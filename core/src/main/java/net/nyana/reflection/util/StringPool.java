package net.nyana.reflection.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class StringPool {
    private final Map<String, String> map;

    public StringPool() { this(4096); }

    public StringPool(int expectedSize) {
        this.map = new HashMap<>(expectedSize, 0.5f);   // 容量/负载因子收进来,不外泄给调用方
    }

    public String intern(final String key) {
        return this.map.computeIfAbsent(key, Function.identity());
    }
}
