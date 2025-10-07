package games.polarbearbytes.walktheline.config;

import games.polarbearbytes.walktheline.state.LockedAxisData;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

@Config(name = "walk-the-line-client")
public class WalkTheLineClientConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public boolean rotatingColor = true;
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min=1, max=100)
    public int rotatingColorAlpha = 100;
    @ConfigEntry.Gui.Tooltip
    public String singleColor = "#FF0000FF";
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min=1, max=100)
    public int lineWidth = 25;

    @ConfigEntry.Gui.Excluded
    private static final Map<RegistryKey<World>, LockedAxisData> CLIENT_LOCKED_AXES = new HashMap<>();
    @ConfigEntry.Gui.Excluded
    public static double tolerance = 0.5;
    @ConfigEntry.Gui.Excluded
    public static boolean modEnabled = false;

    public static LockedAxisData getLockedAxisData(World world){
        return CLIENT_LOCKED_AXES.get(world.getRegistryKey());
    }
    public static void setLockedAxisData(LockedAxisData data, RegistryKey<World> worldKey){
        CLIENT_LOCKED_AXES.put(worldKey, data);
    }

    public static WalkTheLineClientConfig getConfig(){
        return AutoConfig.getConfigHolder(WalkTheLineClientConfig.class).getConfig();
    }

    public static void reset(){
        modEnabled = false;
        tolerance = 0.5;
        CLIENT_LOCKED_AXES.clear();
    }

    public static void register(){
        AutoConfig.register(WalkTheLineClientConfig.class, GsonConfigSerializer::new);
    }
}
