package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Avoid expensive canCreateStructure work when structure placement already forbids this chunk
 * (inspired by 24w04a / MC-249136 commentary).
 */
@Mixin(StructureCheck.class)
public abstract class StructureCheckFastMixin implements IStructureCheck {

    @Shadow
    @Final
    private Registry<Structure> structureConfigs;

    @Unique
    private ChunkGeneratorStructureState omnifix$structureState;

    @Override
    public void omnifix$setStructureState(ChunkGeneratorStructureState state) {
        this.omnifix$structureState = state;
    }

    @ModifyExpressionValue(
            method = "checkStart",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/structure/StructureCheck;tryLoadFromStorage(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/levelgen/structure/Structure;ZJ)Lnet/minecraft/world/level/levelgen/structure/StructureCheckResult;"
            )
    )
    private StructureCheckResult omnifix$earlyExitIfPlacementForbids(
            StructureCheckResult storageResult,
            ChunkPos chunkPos,
            Structure structure,
            boolean skipKnownStructures
    ) {
        if (storageResult != null) {
            return storageResult;
        }
        if (omnifix$structureState == null) {
            return null;
        }
        var structureHolder = this.structureConfigs.wrapAsHolder(structure);
        for (var placement : omnifix$structureState.getPlacementsForStructure(structureHolder)) {
            if (placement.isStructureChunk(omnifix$structureState, chunkPos.x, chunkPos.z)) {
                return null; // allowed — continue vanilla check
            }
        }
        return StructureCheckResult.START_NOT_PRESENT;
    }
}
