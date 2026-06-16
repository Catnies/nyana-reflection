package net.nyana.reflection.remapper;

import java.util.Map;

public record ClassData(
        String deobf,
        String obf,
        Map<String, String> fields,
        Map<String, String> methods
) {
}
