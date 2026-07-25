package net.nyana.reflection.minecraft.remapper;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.nyana.reflection.clazz.NyanaClass;
import net.nyana.reflection.minecraft.VersionHelper;
import net.nyana.reflection.remapper.Remapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;


public final class RemapperHelper {
    private RemapperHelper() {
    }

    private static Remapper create(Map<String, ClassMapping> deobf, Map<String, ClassMapping> obf) {
        return new MappingRemapper(deobf, obf);
    }

    private static Remapper create(Path mappingsFile, String fromNamespace, String toNamespace) throws IOException {
        try (InputStream is = Files.newInputStream(mappingsFile)) {
            return new MappingRemapper(is, fromNamespace, toNamespace);
        }
    }

    private static Remapper create(InputStream mappingsStream, String fromNamespace, String toNamespace) throws IOException {
        return new MappingRemapper(mappingsStream, fromNamespace, toNamespace);
    }

    private static String firstLine(final InputStream is) {
        try {
            is.mark(1024);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            final String line = reader.readLine();
            is.reset();
            return line;
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to read first line of input stream", e);
        }
    }

    public static Remapper createFromPaperJar() {
        // mojang mappings
        if (VersionHelper.mojmapMapping) {
            return Remapper.noOp();
        }

        Class<?> minecraftClass = NyanaClass.find(
                "net.minecraft.obfuscate.DontObfuscate",
                "net.minecraft.server.Main"
        );
        if (minecraftClass == null) {
            return Remapper.noOp();
        }
        // no obf version
        try (InputStream is = minecraftClass.getClassLoader().getResourceAsStream("version.json")) {
            if (is != null) {
                JsonObject json = new Gson().fromJson(new String(is.readAllBytes(), StandardCharsets.UTF_8), JsonObject.class);
                if (json.get("world_version").getAsInt() >= 4764) {
                    return Remapper.noOp();
                }
            }
        } catch (Throwable ignored) {
        } // ignore any errors

        try (InputStream is = minecraftClass.getClassLoader().getResourceAsStream("META-INF/mappings/reobf.tiny")) {
            if (is == null) {
                return Remapper.noOp(); // mojmap version
            }
            InputStream bis = is instanceof BufferedInputStream ? is : new BufferedInputStream(is);

            if (firstLine(bis).contains(MappingNamespaces.MOJANG_PLUS_YARN)) {
                return create(bis, MappingNamespaces.MOJANG_PLUS_YARN, MappingNamespaces.SPIGOT);
            }
            return create(bis, MappingNamespaces.MOJANG, MappingNamespaces.SPIGOT);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read META-INF/mappings/reobf.tiny", e);
        }
    }
}
