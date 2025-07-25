package games.polarbearbytes.walktheline.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import games.polarbearbytes.walktheline.state.LockedAxisData;
import games.polarbearbytes.walktheline.state.PlayerState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;

import static games.polarbearbytes.walktheline.WalkTheLine.MOD_ID;

/**
 * Network Packet for syncing locked data and mod enabled flag
 *
 * @param worldKey The registry key for the world (dimension)
 * @param data The locked axis, coordinate data
 * @param enabled Is the mod enabled
 */
public record SyncPacket(RegistryKey<World> worldKey, LockedAxisData data, Boolean enabled)
        implements CustomPayload {

    public static final Id<SyncPacket> ID =
            new Id<>(Identifier.of(MOD_ID, "sync_locked_axis"));

    //Codec for serialization, deserialization
    //Is this needed? Other records use a CODEC, but we have custom read write codec below, which one is required?
    public static final Codec<SyncPacket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryKey.createCodec(RegistryKeys.WORLD).fieldOf("world").forGetter(SyncPacket::worldKey),
            LockedAxisData.CODEC.fieldOf("data").forGetter(SyncPacket::data),
            Codec.BOOL.fieldOf("enabled").forGetter(SyncPacket::enabled)
    ).apply(instance, SyncPacket::new));

    public static final PacketCodec<PacketByteBuf, SyncPacket> PACKET_CODEC =
            PacketCodec.of(
                    (packet, buf) -> {
                        buf.writeIdentifier(packet.worldKey.getValue());
                        buf.writeEnumConstant(packet.data().axis());
                        buf.writeDouble(packet.data().coordinate());
                        buf.writeBoolean(packet.enabled());
                    },
                    (buf) -> {
                        Identifier worldId = buf.readIdentifier();
                        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, worldId);
                        Axis axis = buf.readEnumConstant(Axis.class);
                        double coordinate = buf.readDouble();
                        Boolean enabled = buf.readBoolean();
                        return new SyncPacket(worldKey, new LockedAxisData(axis, coordinate), enabled);
                    }
            );

    public SyncPacket(ServerPlayerEntity player, Boolean enabled) {
        this(player.getWorld().getRegistryKey(),PlayerState.get().getLockedAxisData(player),enabled);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}