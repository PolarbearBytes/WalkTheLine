package games.polarbearbytes.walktheline.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Player state class for saving the locked axis, coordinate data per world (dimension), per save
 */
public record SavesData(ConcurrentHashMap<String,WorldsData> savesData) {

    public static final Codec<SavesData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(
                Codec.STRING,
                WorldsData.CODEC
            ).fieldOf("saves").forGetter(SavesData::getRawMap)
    ).apply(instance, SavesData::new));

    private SavesData(Map<String, WorldsData> data){
        this(new ConcurrentHashMap<>(data));
    }

    private ConcurrentHashMap<String, WorldsData> getRawMap() {
        return new ConcurrentHashMap<>(this.savesData);
    }
}
