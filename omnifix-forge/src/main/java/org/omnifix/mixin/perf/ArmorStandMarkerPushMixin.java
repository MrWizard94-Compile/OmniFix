package org.omnifix.mixin.perf;

import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Root cause: marker armor stands (map art, holograms) still run
 * {@link ArmorStand#pushEntities} every tick. Vanilla always performs
 * {@code Level#getEntities} with the {@code RIDABLE_MINECARTS} predicate even
 * though markers already report non-pushable / {@code PushReaction.IGNORE} for
 * pistons and never participate in normal entity collision.
 *
 * <p>Approach: cancel {@code pushEntities} at HEAD when {@link ArmorStand#isMarker()}
 * is true. Non-marker stands keep the full minecart push path unchanged.
 * {@code isMarker()} is public on {@link ArmorStand} (DATA_CLIENT_FLAGS bit 16).
 *
 * <p>Trade-off: marker stands never push ridable minecarts. Markers are already
 * non-pushable and ignore pistons; map-art / hologram setups do not rely on
 * stand→minecart push. Non-marker stands are unaffected.
 *
 * <p>Unit: {@code perf.armor_stand_marker_push}
 */
@Mixin(ArmorStand.class)
public abstract class ArmorStandMarkerPushMixin {

    @Inject(method = "pushEntities", at = @At("HEAD"), cancellable = true)
    private void omnifix$skipMarkerPush(CallbackInfo ci) {
        if (((ArmorStand) (Object) this).isMarker()) {
            ci.cancel();
        }
    }
}
