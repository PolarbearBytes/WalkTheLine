package games.polarbearbytes.walktheline.movement;

import com.mojang.datafixers.util.Pair;
import games.polarbearbytes.walktheline.config.ConfigManager;
import games.polarbearbytes.walktheline.network.SyncPacket;
import games.polarbearbytes.walktheline.state.LockedAxisData;
import games.polarbearbytes.walktheline.state.PlayerState;
import games.polarbearbytes.walktheline.state.WorldsData;
import games.polarbearbytes.walktheline.util.Utils;
import games.polarbearbytes.walktheline.world.StrongholdLocator;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;

/**
 * Class for managing what axis and coordinate we are locked on per world (dimension)
 */
public class AxisLockManager {
    //The side to side tolerance for the player to move across the locked axis before being pushed back
    private static final double tolerance = ConfigManager.getConfig().coordinateTolerance;
    //The distance away from locked coordinate to do a teleport instead of doing a pushback
    private static final double teleportTolerance = ConfigManager.getConfig().teleportTolerance;

    /*
     * Register the server events we need to hook into
     */
     public static void register() {
        /*
        When the player changes world (dimension) we need to send a new sync packet to the
        client so it gets the correct locked axis and coordinate for line rendering purposes,
        along with wither or not the mod is enabled
         */
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, from, to) -> {
            WorldsData worldsData = PlayerState.get().getWorldsData(player);
            LockedAxisData lockedAxisData = PlayerState.get().getLockedAxisData(player);
            syncToClient(player,to.getRegistryKey(),lockedAxisData,worldsData.enabled());
        });

        /*
        Same as the dimension change event above but for when player joins, but
        we return early if we haven't any locked data (e.g., when first creating / joining a game)
         */
        ServerPlayerEvents.JOIN.register(player -> {
            WorldsData worldsData = PlayerState.get().getWorldsData(player);
            RegistryKey<World> worldKey = player.getEntityWorld().getRegistryKey();
            LockedAxisData lockedAxisData = worldsData.worldData().get(worldKey);

            if(lockedAxisData == null) return;
            syncToClient(player,worldKey,lockedAxisData,worldsData.enabled());
        });

        /*
        Tick the handler player locked ot axis checking
         */
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                AxisLockManager.handlePlayerTick(player);
            }
        });
    }

    /**
     * Every playertick get locked axis data (if made) and check to make sure player stays within
     *
     * @param player The server entity representing the player
     */
    public static void handlePlayerTick(PlayerEntity player) {
        if (player.isRemoved() || !player.isAlive() || player.getEntityWorld().isClient() || player.isSleeping()) return;
        if(!PlayerState.get().getEnabled((ServerPlayerEntity) player)) return;
        LockedAxisData lockedAxisData = PlayerState.get().getLockedAxisData((ServerPlayerEntity) player);
        if(lockedAxisData == null) return;
        checkDistanceFromLockedAxis((ServerPlayerEntity) player, lockedAxisData);
    }

    /**
     * Calculate how far the player is from the locked axis and push back or teleport when necessary
     *
     * @param player The server entity representing the player
     * @param data The locked axis and coordinate data
     */
    private static void checkDistanceFromLockedAxis(ServerPlayerEntity player, LockedAxisData data){
        double coordinate = Utils.getPlayerCoordAlongLockedAxis(player, data.axis());
        double distance = coordinate - data.coordinate();
        Entity entity;

        /*
        Check if we are riding a vehicle (boat, cart, strider, etc.)
        If so that is the entity we will apply pushback or teleport to
         */
        if (player.hasVehicle()) {
            entity = player.getVehicle();
        } else {
            entity = player;
        }
        if(entity == null) return;
        if (Math.abs(distance) <= tolerance) {
            //We are within the bounds so no need to do anything
            return;
        } else if(Math.abs(distance) >= teleportTolerance){
            //Past the teleport tolerance, so teleport entity back to the locked coordinate
            ServerWorld world = (ServerWorld) entity.getEntityWorld();
            Vec3d pos = entity.getEntityPos();
            player.setVelocity(0.0f,0.0f,0.0f);
            switch(data.axis()){
                case X -> {
                    Vec3d newPos = new Vec3d(data.coordinate(), pos.getY(), pos.getZ());
                    double y = findSafeYAbove(player, newPos);
                    entity.teleport(world, newPos.getX(), y, newPos.getZ() ,EnumSet.noneOf(PositionFlag.class),player.getYaw(),player.getPitch(),false);
                }
                case Z -> {
                    Vec3d newPos = new Vec3d(pos.getX(), pos.getY(), data.coordinate());
                    double y = findSafeYAbove(player, newPos);
                    entity.teleport(world, newPos.getX(), y, newPos.getZ(),EnumSet.noneOf(PositionFlag.class),player.getYaw(),player.getPitch(),false);
                }
            }
            return;
        }
        //Entity is outside the tolerated bounds so apply a small pushback to keep them within
        applyPushback(entity,data.axis(),distance);
    }

    /**
     * Method for applying a pushback on the entity to keep them contained within the locked axis coordinate
     *
     * @param player The entity that is restricted to the locked axis
     * @param axis The axis they are restricted to
     * @param distance The distance away from the locked coordinate
     */
    private static void applyPushback(Entity player, Axis axis, double distance){
        Vec3d velocity = player.getVelocity();

        double overshoot = Math.abs(distance) - tolerance;
        double direction = -Math.signum(distance);
        double velocityAmount = direction * overshoot * 0.3;

        Vec3d newVelocity = switch (axis) {
            case X -> new Vec3d(velocityAmount, velocity.y, velocity.z);
            case Z -> new Vec3d(velocity.x, velocity.y, velocityAmount);
            default -> throw new IllegalStateException("Unexpected value: " + axis);
        };

        player.setVelocity(newVelocity);
        player.velocityModified = true;
    }

    /**
     * When teleporting player find a safe space to teleport player
     * Block beneath player needs to not be air, block at foot and head level need to be air
     *
     * @param player The server entity representing the player
     * @param position The position we want to teleport to
     * @return The Y coordinate that we have determined to be safe
     */
    private static double findSafeYAbove(ServerPlayerEntity player, Vec3d position) {
        ServerWorld world = player.getEntityWorld();
        BlockPos.Mutable mutablePosition = new BlockPos.Mutable((int) Math.floor(position.getX()), world.getHeight(), (int) Math.floor(position.getZ()));
        int bottom = world.getBottomY();
        boolean isHeadAir = world.getBlockState(mutablePosition).isAir();
        boolean isFootAir = world.getBlockState(mutablePosition.move(Direction.DOWN)).isAir();
        boolean isBelowAir;

        //scan from sky downwards
        while(mutablePosition.getY() >= bottom) {
            isBelowAir = world.getBlockState(mutablePosition.move(Direction.DOWN)).isAir();
            if (!isBelowAir && isFootAir && isHeadAir) {
                return mutablePosition.getY() + 1;
            }
            isHeadAir = isFootAir;
            isFootAir = isBelowAir;
        }
        // Fallback to original position
        return position.getY();
    }

    /**
     * Method for getting the locked axis for the world (dimension) passed in
     * Overworld: locate stronghold and make sure the locked coordinate passes through the center of the portal frame
     * Nether: determine based on where we first teleport into the nether (probably need to tweak this for when changing portals)
     * End: hard coded as the locked axis should always be Z and on coordinate 0
     *
     * @param player The server entity representing the player
     * @param worldKey The registry key for the world (dimension)
     * @return The locked axis and coordinate for that world
     */
    public static LockedAxisData determineDimensionLocks(ServerPlayerEntity player, RegistryKey<World> worldKey) {
        PlayerState state = PlayerState.get();
        MinecraftServer server = player.getEntityWorld().getServer();
        if(server == null) return null;
        String saveName = server.getSaveProperties().getLevelName();
        Axis axis;
        double coordinate;

        switch (worldKey.getValue().getPath()) {
            case "overworld" -> {
                //Get the overworld's spawn location
                BlockPos spawnPosition = player.getEntityWorld().getSpawnPoint().getPos();

                //Find the closest stronghold and return position and axis
                Pair<BlockPos, Direction> locationPair = StrongholdLocator.getClosestStrongHoldPortalroom(spawnPosition);
                if(locationPair == null || locationPair.getFirst() == null) return null;
                BlockPos pos = locationPair.getFirst();


                axis = locationPair.getSecond().getAxis();
                /*
                Flip the axis so that we will be along the path that goes through
                the silverfish spawner and portalframe
                 */
                if (axis == Axis.X) {
                    axis = Axis.Z;
                } else {
                    axis = Axis.X;
                }

                coordinate = pos.getComponentAlongAxis(axis) + 0.5d;
            }
            case "the_nether" -> {
                ServerWorld nether = player.getEntityWorld().getServer().getWorld(World.NETHER);
                if(nether == null) return null;
                LockedAxisData data = state.getLockedAxisData(player,saveName, World.OVERWORLD);
                axis = data.axis();
                coordinate = Math.floor(player.getEntityPos().getComponentAlongAxis(axis)) + 0.5d;
            }
            //the end dimension
            default -> {
                axis = Axis.Z;
                coordinate = 0.5d;
            }
        }
        return new LockedAxisData(axis, coordinate);
    }

    /**
     * Send a packet to the client side so it can know where to display
     * the locked axis line indicator
     *
     * @param player The server entity representing the player
     * @param worldKey The registry key for the world (dimension)
     * @param data The locked axis and coordinate data
     * @param enabled Boolean determine if the mod is enabled
     */
    public static void syncToClient(ServerPlayerEntity player, RegistryKey<World> worldKey, LockedAxisData data, Boolean enabled) {
        SyncPacket packet = new SyncPacket(worldKey,data, enabled);
        ServerPlayNetworking.send(player, packet);
    }
}
