package games.polarbearbytes.walktheline.world;

import com.mojang.datafixers.util.Pair;
import games.polarbearbytes.walktheline.WalkTheLine;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.structure.Structure;

/**
 * Class for locating the nearest stronghold based on passed location
 */
public class StrongholdLocator {

    /**
     * Gets the registry list of stronghold structures
     * @return List of stronghold structures
     */
    public static RegistryEntryList<Structure> getStrongholdList(){
        //long seed = WalkTheLine.server.getOverworld().getSeed();

        RegistryWrapper.WrapperLookup registryManager = WalkTheLine.server.getOverworld().getRegistryManager();
        RegistryEntryLookup<Structure> structureRegistry = registryManager.getOrThrow(RegistryKeys.STRUCTURE);

        RegistryKey<Structure> strongholdKey = RegistryKey.of(RegistryKeys.STRUCTURE, Identifier.of("stronghold"));
        RegistryEntry<Structure> strongholdEntry = structureRegistry.getOptional(strongholdKey)
                .orElseThrow(() -> new IllegalStateException("Stronghold not found in registry"));

        return RegistryEntryList.of(strongholdEntry);
    }

    /**
     * Using the passed position, find closest stronghole, traverse the StrongholdStructure children pieces
     * to find the portal room and coordinates for the portal frame
     *
     * @param locationPos The location to search from for closest Stronghold
     * @return Pair containing the position of the portal frame and its facing direction
     */
    public static Pair<BlockPos, Direction> getClosestStrongHoldPortalroom(BlockPos locationPos){
        ServerWorld serverWorld = WalkTheLine.server.getOverworld();
        RegistryEntryList<Structure> list = getStrongholdList();
        /*
        Calls the internal locating code, /locate structure stronghold uses this
        Gives the location of the Start Structure.Piece, along with the Structure object
         */
        Pair<BlockPos, RegistryEntry<Structure>> pair = serverWorld.getChunkManager()
                .getChunkGenerator()
                .locateStructure(serverWorld, list, locationPos, 100, false);
        if(pair == null){
            //Should this be a thrown exception?
            WalkTheLine.LOGGER.error("No Stronghold Structures Found");
            return null;
        } else {
            BlockPos pos = pair.getFirst();
            RegistryEntry<Structure> found = pair.getSecond();

            /*
            Used to load the chunk such that the stronghold pieces also get generated / loaded
            Pieces need to have been generated / loaded in order for use to traverse them to find
            the portal room piece.
             */
            StructureAccessor structureAccessor = serverWorld.getStructureAccessor();
            StructureStart structureStart = structureAccessor.getStructureAt(pos, found.value());

            if (structureStart == null || !structureStart.hasChildren()) {
                // No structure generated yet or failed to load
                WalkTheLine.LOGGER.error("Failed to load stronghold's chunk?");
                return null;
            }

            /*
            Loop through all the pieces till we find the one that has
            the PortalRoom instance, from there calculate center of room
            and then offset 2 in the direction room is facing to get
            the coordinate that would be within the portal frame
             */
            for (StructurePiece piece : structureStart.getChildren()) {
                if (piece instanceof StrongholdGenerator.PortalRoom) {
                    Direction facingDirection = piece.getFacing();
                    BlockBox box = piece.getBoundingBox();
                    BlockPos center =  new BlockPos(
                            (box.getMinX() + box.getMaxX()) / 2,
                            (box.getMinY() + box.getMaxY()) / 2,
                            (box.getMinZ() + box.getMaxZ()) / 2
                    ).offset(piece.getFacing(),2);
                    return new Pair<>(center, facingDirection);
                }
            }
        }
        return null;
    }
}