package net.nyana.reflection.minecraft;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.nyana.reflection.clazz.NyanaClass;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class VersionHelper {
    private VersionHelper() {
    }

    public static final int version;
    public static final boolean mojmapMapping;
    public static final boolean hasSpigotPatch;
    public static final boolean hasFoliaPatch;
    public static final boolean hasPaperPatch;
    public static final boolean hasLeavesPatch;
    public static final boolean hasCanvasPatch;
    public static final boolean hasLeafPatch;
    public static final boolean hasLithiumPatch;
    private static final Class<?> UNOBFUSCATED_CLAZZ = Objects.requireNonNull(NyanaClass.find(
            "net.minecraft.obfuscate.DontObfuscate",
            "net.minecraft.data.Main",
            "net.minecraft.server.Main",
            "net.minecraft.gametest.Main",
            "net.minecraft.client.main.Main",
            "net.minecraft.client.data.Main"
    ));

    static {
        try (InputStream inputStream = UNOBFUSCATED_CLAZZ.getResourceAsStream("/version.json")) {
            if (inputStream == null) {
                throw new IOException("Failed to load version.json");
            }

            JsonObject json = new Gson().fromJson(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8), JsonObject.class);
            String versionString = json.getAsJsonPrimitive("id").getAsString()
                    .split("-", 2)[0]  // 1.21.10-rc1          -> 1.21.10
                    .split("_", 2)[0]; // 1.21.11_unobfuscated -> 1.21.11

            // 12001 = 1.20.1
            // 260100 = 26.1
            version = parseVersionToInteger(versionString);

            mojmapMapping = checkMojMap() || version >= 260100;

            hasSpigotPatch = checkSpigot();
            hasFoliaPatch = checkFolia();
            hasPaperPatch = checkPaper();
            hasLeavesPatch = checkLeaves();
            hasCanvasPatch = checkCanvas();
            hasLeafPatch = checkLeaf();
            hasLithiumPatch = checkLithium();
        } catch (Exception e) {
            throw new RuntimeException("Failed to init VersionHelper", e);
        }
    }

    private static boolean exists(String... classNames) {
        for (String className : classNames) {
            try {
                Class.forName(className.replace("{}", "."), false, VersionHelper.class.getClassLoader());
                return true;
            } catch (ClassNotFoundException ignored) {
            }
        }
        return false;
    }

    private static boolean checkMojMap() {
        return exists("net.neoforged.art.internal.RenamerImpl");
    }

    private static boolean checkSpigot() {
        return exists("org.spigotmc.SpigotConfig");
    }

    private static boolean checkFolia() {
        return exists("io.papermc.paper.threadedregions.RegionizedServer");
    }

    private static boolean checkPaper() {
        return exists("io.papermc.paper.adventure.PaperAdventure");
    }

    private static boolean checkLeaves() {
        return exists("org.leavesmc.leaves.bot.BotList");
    }

    private static boolean checkCanvas() {
        return exists("io.canvasmc.canvas.Config") || exists("io.canvasmc.canvas.GlobalConfiguration");
    }

    private static boolean checkLeaf() {
        return exists("org.dreeam.leaf.config.LeafConfig");
    }

    private static boolean checkLithium() {
        return exists("net.caffeinemc.mods.lithium.common.world.chunk.LithiumHashPalette");
    }

    public static int parseVersionToInteger(String versionString) {
        int v1 = 0;
        int v2 = 0;
        int v3 = 0;
        int currentNumber = 0;
        int part = 0;

        for (int i = 0; i < versionString.length(); i++) {
            char c = versionString.charAt(i);
            if (c >= '0' && c <= '9') {
                currentNumber = currentNumber * 10 + (c - '0');
                continue;
            }

            if (c == '.') {
                if (part == 0) {
                    v1 = currentNumber;
                } else if (part == 1) {
                    v2 = currentNumber;
                }

                part++;
                currentNumber = 0;
                if (part > 2) break;
            }
        }

        if (part == 0) {
            v1 = currentNumber;
        } else if (part == 1) {
            v2 = currentNumber;
        } else if (part == 2) {
            v3 = currentNumber;
        }

        return v1 * 10000 + v2 * 100 + v3;
    }
}
