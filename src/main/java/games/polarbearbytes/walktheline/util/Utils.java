package games.polarbearbytes.walktheline.util;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction.Axis;

/**
 * Utility functions
 */
public class Utils {
    public static double getPlayerCoordAlongLockedAxis(PlayerEntity player, Axis axis) {
        return (axis == Axis.X) ? player.getX() : player.getZ();
    }
    public static void debugMessage(ClientPlayerEntity player, String msg) {
        player.sendMessage(Text.literal(msg), true);
    }
}
