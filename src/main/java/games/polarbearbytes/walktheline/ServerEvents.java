package games.polarbearbytes.walktheline;

import games.polarbearbytes.walktheline.movement.AxisLockManager;
import games.polarbearbytes.walktheline.network.SyncPacket;
import games.polarbearbytes.walktheline.state.PlayerState;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * One stop shop for our events for events that are not class specific
 */
public class ServerEvents {
    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register((server)->WalkTheLine.server=server);
        ServerWorldEvents.LOAD.register((server, world) -> WalkTheLine.server = server);
        AxisLockManager.register();
        PayloadTypeRegistry.playS2C().register(SyncPacket.PAYLOAD_ID, SyncPacket.PACKET_CODEC);

        //TODO: Make enabling permanent so that player cant "cheat" and disable do stuff and then re-enable
        /*
        Our command for enabling / disabling the mod for a save
        Probably change to a keybinding maybe, also locking the mod
        to enabled when the mod is first enabled to prevent players
        from being able to disable mod, do stuff and then re-enable
         */
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                literal("walktheline")
                        .then(literal("enable")
                                .executes(ctx -> {
                                    PlayerState.get().setEnabled(ctx.getSource().getPlayer(),true);
                                    ctx.getSource().sendFeedback(() -> Text.literal("Walk the Line enabled."), false);
                                    return 1;
                                })
                        )
                        .then(literal("disable")
                                .executes(ctx -> {
                                    PlayerState.get().setEnabled(ctx.getSource().getPlayer(),false);
                                    ctx.getSource().sendFeedback(() -> Text.literal("Walk the Line disabled."), false);
                                    return 1;
                                })
                        )
        ));
    }
}
