package org.omnifix.compat.createportals.mixin;

import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import qouteall.imm_ptl.core.api.ImmPtlEntityExtension;

/**
 * Create owns train portal transit via dimensional carriages and zero-length track-graph portal edges.
 * Immersive Portals must not teleport the carriage entity itself or the train graph and far-side
 * entity pair fall out of sync.
 *
 * <p>Same extension hook IP uses for {@code EnderDragon} / fishing hooks.
 */
@Mixin(CarriageContraptionEntity.class)
public abstract class MixinCarriageContraptionEntityNoIpTeleport implements ImmPtlEntityExtension {

    @Override
    public boolean imm_ptl_canTeleportThroughPortal(Entity portal) {
        return false;
    }
}
