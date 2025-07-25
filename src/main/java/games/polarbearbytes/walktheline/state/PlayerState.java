package games.polarbearbytes.walktheline.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import games.polarbearbytes.walktheline.WalkTheLine;
import games.polarbearbytes.walktheline.network.SyncPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static games.polarbearbytes.walktheline.movement.AxisLockManager.determineDimensionLocks;

/**
 * Player state class for saving the locked axis, coordinate data per world (dimension), per save, per player
 */
public class PlayerState extends PersistentState {
    public static final Codec<PlayerState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(
                    Uuids.CODEC,
                    SavesData.CODEC
            ).fieldOf("players").forGetter(PlayerState::getRawMap)
    ).apply(instance, PlayerState::new));

    public static final PersistentStateType<PlayerState> TYPE = new PersistentStateType<>("walk_the_line_state",PlayerState::new, CODEC, DataFixTypes.PLAYER);

    private HashMap<UUID, SavesData> playersData = new HashMap<>();

    public PlayerState() {}

    public PlayerState(Map<UUID, SavesData> playersData) {
        this.playersData = new HashMap<>();
        this.playersData.putAll(playersData);
    }

    public static PlayerState get() {
        return WalkTheLine.server.getOverworld().getPersistentStateManager().getOrCreate(TYPE);
    }

    public SavesData getPlayerSaves(ServerPlayerEntity player){
        return playersData.computeIfAbsent(player.getUuid(),(uuid)-> new SavesData(new ConcurrentHashMap<>()));
    }
    public WorldsData getWorldsData(ServerPlayerEntity player, String saveName){
        return getPlayerSaves(player).savesData().computeIfAbsent(saveName,(savedName)-> new WorldsData(new ConcurrentHashMap<>(),false));
    }
    public LockedAxisData getLockedAxisData(ServerPlayerEntity player, String saveName, RegistryKey<World> worldKey){
        return getWorldsData(player,saveName).worldData().computeIfAbsent(worldKey,(worlds)-> determineDimensionLocks(player,worldKey));
    }

    public WorldsData getWorldsData(ServerPlayerEntity player){
        String saveName = Objects.requireNonNull(player.getServer()).getSaveProperties().getLevelName();
        return getWorldsData(player,saveName);
    }
    public LockedAxisData getLockedAxisData(ServerPlayerEntity player){
        String saveName = Objects.requireNonNull(player.getServer()).getSaveProperties().getLevelName();
        RegistryKey<World> worldKey = player.getWorld().getRegistryKey();
        return getLockedAxisData(player,saveName,worldKey);
    }

    public void setEnabled(ServerPlayerEntity player, Boolean enabled){
        MinecraftServer server = player.getServer();
        if(server == null) return;
        String saveName = server.getSaveProperties().getLevelName();
        getPlayerSaves(player).savesData().compute(saveName,
                (savedName,worldsData)->{
                    if(worldsData == null){
                        return new WorldsData(new ConcurrentHashMap<>(),enabled);
                    }
                    return new WorldsData(worldsData.worldData(),enabled);
                });
        markDirty();

        SyncPacket packet = new SyncPacket(player,enabled);
        ServerPlayNetworking.send(player, packet);
    }

    public boolean getEnabled(ServerPlayerEntity player){
        return getWorldsData(player).enabled();
    }

    private Map<UUID, SavesData> getRawMap() {
        return new HashMap<>(this.playersData);
    }
}