package net.nyana.reflection.remapper;

import java.util.Map;

record ClassMapping(
        String sourceClassName,
        String targetClassName,
        Map<String, String> fieldsBySourceName,
        Map<String, String> methodsBySourceNameAndTargetParams
) {
}
