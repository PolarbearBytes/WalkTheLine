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
    public static int colorHexToInt(String colorHex){
        String[] rgba = colorHex.split("(?<=\\G.{2})");
        int r = Integer.parseInt(rgba[0],16);
        int g = Integer.parseInt(rgba[1],16);
        int b = Integer.parseInt(rgba[2],16);
        int a = Integer.parseInt(rgba[3],16);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
