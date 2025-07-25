package games.polarbearbytes.walktheline.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Direction.Axis;

/**
 * Utility functions
 */
public class Utils {
    public static double getPlayerCoordAlongLockedAxis(PlayerEntity player, Axis axis) {
        return (axis == Axis.X) ? player.getX() : player.getZ();
    }
}
