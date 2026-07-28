package org.omnifix.mixin.perf;

import net.minecraft.core.BlockPos;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * When PotentialSpawns listeners did not mutate the list, reuse the original WeightedRandomList.
 */
@Mixin(ForgeEventFactory.class)
public abstract class ForgeEventFactoryPotentialSpawnsMixin {

    @Redirect(
            method = "getPotentialSpawns",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/random/WeightedRandomList;create(Ljava/util/List;)Lnet/minecraft/util/random/WeightedRandomList;"
            )
    )
    private static WeightedRandomList<MobSpawnSettings.SpawnerData> omnifix$reuseIfUnmodified(
            List<MobSpawnSettings.SpawnerData> items,
            LevelAccessor level,
            MobCategory category,
            BlockPos pos,
            WeightedRandomList<MobSpawnSettings.SpawnerData> oldList
    ) {
        if (items == oldList.unwrap()) {
            return oldList;
        }
        return WeightedRandomList.create(items);
    }
}
