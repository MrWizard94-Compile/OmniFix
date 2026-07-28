package org.omnifix.mixin.perf;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.omnifix.worldgen.ExtendedSurfaceContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;

@Mixin(SurfaceRules.BiomeConditionSource.class)
public abstract class BiomeConditionSourceOptimizeMixin {

    @Shadow
    @Final
    public List<ResourceKey<Biome>> biomes;

    @Inject(
            method = "apply(Lnet/minecraft/world/level/levelgen/SurfaceRules$Context;)Lnet/minecraft/world/level/levelgen/SurfaceRules$Condition;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void omnifix$optimizeCondition(
            SurfaceRules.Context context,
            CallbackInfoReturnable<SurfaceRules.Condition> cir
    ) {
        var possibleBiomes = ((ExtendedSurfaceContext) (Object) context).omnifix$getPossibleBiomes();
        if (possibleBiomes != null) {
            if (omnifix$guaranteedNoMatch(possibleBiomes)) {
                cir.setReturnValue(() -> false);
            } else if (omnifix$alwaysMatches(possibleBiomes)) {
                cir.setReturnValue(() -> true);
            }
        }
    }

    @Unique
    private boolean omnifix$guaranteedNoMatch(Set<ResourceKey<Biome>> possible) {
        List<ResourceKey<Biome>> test = this.biomes;
        for (int i = 0; i < test.size(); i++) {
            if (possible.contains(test.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Unique
    private boolean omnifix$alwaysMatches(Set<ResourceKey<Biome>> possible) {
        List<ResourceKey<Biome>> test = this.biomes;
        for (var biome : possible) {
            if (!test.contains(biome)) {
                return false;
            }
        }
        return true;
    }
}
