package net.nyana.reflection.minecraft.remapper;

import java.util.Map;
import java.util.function.Function;

final class StringPool {
    private final Map<String, String> map;

    StringPool(Map<String, String> map) {
        this.map = map;
    }

    public String get(final String key) {
        return this.map.computeIfAbsent(key, Function.identity());
    }
}