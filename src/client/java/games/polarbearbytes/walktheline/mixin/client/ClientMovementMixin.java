package games.polarbearbytes.walktheline.mixin.client;

import games.polarbearbytes.walktheline.config.WalkTheLineClientConfig;
import games.polarbearbytes.walktheline.state.LockedAxisData;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class ClientMovementMixin {
    @Inject(method = "move", at = @At("RETURN"))
    private void restrictMovement(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if(!self.isPlayer() || !self.getEntityWorld().isClient() || !WalkTheLineClientConfig.modEnabled) return;

        LockedAxisData data = WalkTheLineClientConfig.getLockedAxisData(self.getEntityWorld());
        if(data == null) return;

        double x = self.getX();
        double z = self.getZ();
        double tolerance = WalkTheLineClientConfig.tolerance;

        if(data.axis() == Direction.Axis.X){
            if(x > data.coordinate() + tolerance) x = data.coordinate() + tolerance;
            if(x < data.coordinate() - tolerance) x = data.coordinate() - tolerance;
        } else {
            if(z > data.coordinate() + tolerance) z = data.coordinate() + tolerance;
            if(z < data.coordinate() - tolerance) z = data.coordinate() - tolerance;
        }
        self.setPos(x, self.getY(), z);
    }
}
