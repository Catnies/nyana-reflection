package net.nyana.reflection.minecraft.condition.version;

import net.nyana.reflection.condition.CustomCondition;
import net.nyana.reflection.minecraft.VersionHelper;
import org.jetbrains.annotations.NotNull;

public final class MaxVersion extends CustomCondition {
    @Override
    public boolean check(@NotNull String[] value) {
        if (value.length == 0) {
            return false;
        }

        String version = value[0];
        return VersionHelper.parseVersionToInteger(version) <= VersionHelper.version;
    }
}
