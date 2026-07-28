package org.omnifix.mixin.perf;

import net.minecraft.core.BlockPos;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.event.level.LevelEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * PotentialSpawns allocates ArrayLists every call even when no listener mutates the list.
 * Share the underlying WeightedRandomList unwrap until mutation.
 */
@Mixin(LevelEvent.PotentialSpawns.class)
public abstract class PotentialSpawnsMixin {

    @Shadow(remap = false)
    @Final
    @Mutable
    private List<MobSpawnSettings.SpawnerData> view;

    @Shadow(remap = false)
    @Final
    @Mutable
    private List<MobSpawnSettings.SpawnerData> list;

    @Unique
    private static final ArrayList<MobSpawnSettings.SpawnerData> OMNIFIX$SENTINEL = new ArrayList<>();

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "java/util/ArrayList", ordinal = 1))
    private ArrayList<?> omnifix$avoidListAlloc1() {
        return OMNIFIX$SENTINEL;
    }

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "java/util/ArrayList", ordinal = 0))
    private ArrayList<?> omnifix$avoidListAlloc2(Collection<?> c) {
        return OMNIFIX$SENTINEL;
    }

    @Redirect(
            method = "<init>",
            at = @At(value = "INVOKE", target = "Ljava/util/Collections;unmodifiableList(Ljava/util/List;)Ljava/util/List;")
    )
    private List<?> omnifix$avoidUnmodifiable(List<?> l) {
        return null;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void omnifix$initSmartLists(
            LevelAccessor level,
            MobCategory category,
            BlockPos pos,
            WeightedRandomList<MobSpawnSettings.SpawnerData> oldList,
            CallbackInfo ci
    ) {
        this.view = oldList.unwrap();
        this.list = null;
    }

    @Unique
    private void omnifix$populateList() {
        if (this.list == null) {
            this.list = new ArrayList<>(this.view);
            this.view = Collections.unmodifiableList(this.list);
        }
    }

    @Inject(method = "addSpawnerData", at = @At("HEAD"), remap = false)
    private void omnifix$onAdd(MobSpawnSettings.SpawnerData data, CallbackInfo ci) {
        omnifix$populateList();
    }

    @Inject(method = "removeSpawnerData", at = @At("HEAD"), remap = false)
    private void omnifix$onRemove(MobSpawnSettings.SpawnerData data, CallbackInfoReturnable<Boolean> cir) {
        omnifix$populateList();
    }
}
