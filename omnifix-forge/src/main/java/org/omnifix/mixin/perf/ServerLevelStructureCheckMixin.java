package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Wire {@link ChunkGeneratorStructureState} into StructureCheck for early placement rejection.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelStructureCheckMixin {

    @Shadow
    @Final
    private ServerChunkCache chunkSource;

    @ModifyExpressionValue(
            method = "<init>",
            at = @At(
                    value = "NEW",
                    target = "(Lnet/minecraft/world/level/chunk/storage/ChunkScanAccess;Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/LevelHeightAccessor;Lnet/minecraft/world/level/biome/BiomeSource;JLcom/mojang/datafixers/DataFixer;)Lnet/minecraft/world/level/levelgen/structure/StructureCheck;",
                    ordinal = 0
            )
    )
    private StructureCheck omnifix$attachGeneratorState(StructureCheck check) {
        ((IStructureCheck) check).omnifix$setStructureState(this.chunkSource.getGeneratorState());
        return check;
    }
}
