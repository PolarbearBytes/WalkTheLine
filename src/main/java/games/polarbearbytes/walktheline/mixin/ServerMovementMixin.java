package games.polarbearbytes.walktheline.mixin;

import games.polarbearbytes.walktheline.config.ConfigManager;
import games.polarbearbytes.walktheline.config.WalkTheLineConfig;
import games.polarbearbytes.walktheline.movement.AxisLockManager;
import games.polarbearbytes.walktheline.state.LockedAxisData;
import games.polarbearbytes.walktheline.state.PlayerState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class ServerMovementMixin {
    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void restrictMovement(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if(!self.isPlayer() || self.getEntityWorld().isClient() || !PlayerState.get().getEnabled((ServerPlayerEntity) self)) return;

        LockedAxisData data = PlayerState.get().getLockedAxisData((ServerPlayerEntity) self);
        if(data == null) return;
        WalkTheLineConfig cfg = ConfigManager.getConfig();

        double x = self.getX();
        double z = self.getZ();
        double xv = self.getVelocity().x;
        double zv = self.getVelocity().z;

        if(data.axis() == Direction.Axis.X){
            if(x > data.coordinate() + cfg.coordinateTolerance) {
                x = data.coordinate() + cfg.coordinateTolerance;
                xv = 0;
            }
            if(x < data.coordinate() - cfg.coordinateTolerance){
                x = data.coordinate() - cfg.coordinateTolerance;
                xv = 0;
            }

        } else {
            if(z > data.coordinate() + cfg.coordinateTolerance){
                z = data.coordinate() + cfg.coordinateTolerance;
                zv = 0;
            }
            if(z < data.coordinate() - cfg.coordinateTolerance){
                z = data.coordinate() - cfg.coordinateTolerance;
                zv = 0;
            }
        }

        boolean result = AxisLockManager.checkDistanceFromLockedAxis((ServerPlayerEntity) self,data);

        //result will be false if it had to move us.
        if(result) {
            self.setPosition(x, self.getY(), z);
            self.setVelocity(xv, self.getVelocity().y, zv);
        }
    }
}
