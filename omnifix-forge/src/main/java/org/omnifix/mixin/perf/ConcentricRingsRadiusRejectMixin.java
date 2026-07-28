package org.omnifix.mixin.perf;

import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Early-reject {@code isPlacementChunk} for concentric rings when the chunk is outside the
 * conservative ring radius. Avoids blocking on ring-position futures around spawn (0,0) on new
 * worlds before strongholds are ready.
 */
@Mixin(ConcentricRingsStructurePlacement.class)
public abstract class ConcentricRingsRadiusRejectMixin {

    @Shadow
    @Final
    private int distance;

    @Shadow
    @Final
    private int spread;

    @Shadow
    @Final
    private int count;

    /** Max per-axis section displacement after biome snap (vanilla radius 112 blocks). */
    @Unique
    private static final int OMNIFIX$MAX_BIOME_SNAP_SECTIONS = 7;

    @Unique
    private static final double OMNIFIX$MAX_ROUNDING_ERROR = Math.sqrt(2.0) * 0.5;

    @Unique
    private static final double OMNIFIX$MAX_BIOME_SNAP_ERROR =
            OMNIFIX$MAX_BIOME_SNAP_SECTIONS * Math.sqrt(2.0);

    @Unique
    private static final double OMNIFIX$MAX_POSITION_ERROR =
            OMNIFIX$MAX_ROUNDING_ERROR + OMNIFIX$MAX_BIOME_SNAP_ERROR;

    @Unique
    private long omnifix$innerRadiusSq;

    @Unique
    private long omnifix$outerRadiusSq;

    @Inject(
            method = "<init>(Lnet/minecraft/core/Vec3i;Lnet/minecraft/world/level/levelgen/structure/placement/StructurePlacement$FrequencyReductionMethod;FILjava/util/Optional;IIILnet/minecraft/core/HolderSet;)V",
            at = @At("RETURN")
    )
    private void omnifix$computeRadiusBounds(CallbackInfo ci) {
        double maxNoise = this.distance * 1.25;

        double minDist = 4.0 * this.distance - maxNoise;
        double safeInnerRadius = minDist - OMNIFIX$MAX_POSITION_ERROR;
        this.omnifix$innerRadiusSq = (long) Math.max(0.0, Math.floor(safeInnerRadius * safeInnerRadius));

        if (this.spread == 0) {
            this.omnifix$outerRadiusSq = Long.MAX_VALUE;
            return;
        }

        int maxCircle = omnifix$computeMaxCircleIndex();
        double maxDist = 4.0 * this.distance + (double) this.distance * maxCircle * 6.0 + maxNoise;
        double safeOuterRadius = maxDist + OMNIFIX$MAX_POSITION_ERROR;
        this.omnifix$outerRadiusSq = (long) Math.ceil(safeOuterRadius * safeOuterRadius);
    }

    @Unique
    private int omnifix$computeMaxCircleIndex() {
        int ringSpread = this.spread;
        int total = 0;
        int circle = 0;

        while (total + ringSpread < this.count) {
            total += ringSpread;
            circle++;
            ringSpread += 2 * ringSpread / (circle + 1);
            ringSpread = Math.min(ringSpread, this.count - total);
        }
        return circle;
    }

    @Inject(method = "isPlacementChunk", at = @At("HEAD"), cancellable = true)
    private void omnifix$earlyRejectByRadius(
            ChunkGeneratorStructureState structureState,
            int x,
            int z,
            CallbackInfoReturnable<Boolean> cir
    ) {
        long distSq = (long) x * x + (long) z * z;
        if (distSq < this.omnifix$innerRadiusSq || distSq > this.omnifix$outerRadiusSq) {
            cir.setReturnValue(false);
        }
    }
}
