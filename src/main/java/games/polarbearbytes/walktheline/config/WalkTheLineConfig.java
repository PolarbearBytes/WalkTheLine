package games.polarbearbytes.walktheline.config;

/**
 * Data class for the config
 */
public class WalkTheLineConfig {
    //Tolerance for side to side movement on the locked axis
    public double coordinateTolerance = 0.5;
    //How faraway from the locked axis coordinate to just teleport instead of simply doing a pushback
    public double teleportTolerance = 2;
}