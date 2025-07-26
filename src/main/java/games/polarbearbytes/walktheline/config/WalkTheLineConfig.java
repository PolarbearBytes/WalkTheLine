package games.polarbearbytes.walktheline.config;

/**
 * Data class for the config
 */
public class WalkTheLineConfig {
    //The particle type the user wants for when using ParticleLineRenderer
    public String particleType = "electric_spark";
    //Tolerance for side to side movement on the locked axis
    public double coordinateTolerance = 0.40;
    //How faraway from the locked axis coordinate to just teleport instead of simply doing a pushback
    public double teleportTolerance = 2;

    public int lineLength = 64;
    public int lineColorRotateTiming = 6;
}