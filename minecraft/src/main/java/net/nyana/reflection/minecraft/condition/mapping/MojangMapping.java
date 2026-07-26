package net.nyana.reflection.minecraft.condition.mapping;

import net.nyana.reflection.condition.CustomCondition;
import net.nyana.reflection.minecraft.VersionHelper;
import org.jetbrains.annotations.NotNull;

public final class MojangMapping extends CustomCondition {
    @Override
    public boolean check(@NotNull String[] value) {
        return VersionHelper.mojmapMapping;
    }
}

