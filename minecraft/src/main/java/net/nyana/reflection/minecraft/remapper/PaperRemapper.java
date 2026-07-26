package net.nyana.reflection.minecraft.remapper;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.nyana.reflection.clazz.NyanaClass;
import net.nyana.reflection.minecraft.VersionHelper;
import net.nyana.reflection.remapper.Remapper;

import java.io.*;
import java.nio.charset.StandardCharsets;

public final class PaperRemapper {
    private PaperRemapper() {
    }

    public static Remapper create() {
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
                return Remapper.loadMappingIo(bis, MappingNamespaces.MOJANG_PLUS_YARN, MappingNamespaces.SPIGOT);
            }
            return Remapper.loadMappingIo(bis, MappingNamespaces.MOJANG, MappingNamespaces.SPIGOT);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read META-INF/mappings/reobf.tiny", e);
        }
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
}
