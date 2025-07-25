package games.polarbearbytes.walktheline.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Player state class for saving the locked axis, coordinate data per world (dimension)
 */
public record WorldsData(ConcurrentHashMap<RegistryKey<World>,LockedAxisData> worldData, Boolean enabled) {
    public static final Codec<WorldsData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(
                RegistryKey.createCodec(RegistryKeys.WORLD),
                LockedAxisData.CODEC
            ).fieldOf("worlds").forGetter(WorldsData::getRawMap),
            Codec.BOOL.fieldOf("enabled").forGetter(WorldsData::isEnabled)
    ).apply(instance, WorldsData::new));

    private WorldsData(Map<RegistryKey<World>, LockedAxisData> data, Boolean enabled){
        this(new ConcurrentHashMap<>(data),enabled);
    }

    private Map<RegistryKey<World>, LockedAxisData> getRawMap() {
        return new ConcurrentHashMap<>(this.worldData);
    }

    public boolean isEnabled(){
        return enabled;
    }
}
