package net.nyana.reflection.minecraft.condition.patch;

import net.nyana.reflection.condition.CustomCondition;
import net.nyana.reflection.minecraft.VersionHelper;
import org.jetbrains.annotations.NotNull;

public final class SpigotPatch extends CustomCondition {
    @Override
    public boolean check(@NotNull String[] value) {
        return VersionHelper.hasSpigotPatch;
    }
}

