package org.omnifix.mixin.leak;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Supplier;

/**
 * LDLib {@code DummyWorld} super(Level) call often passes a profiler supplier that captures the
 * source ClientLevel. Rebind to {@link Level#getProfilerSupplier()} of the source world.
 *
 * <p>ATL DummyWorldMixin-class; Pseudo so LDLib is optional.
 */
@Pseudo
@Mixin(targets = "com.lowdragmc.lowdraglib.utils.DummyWorld", remap = false)
public abstract class LdLibDummyWorldMixin {

    @ModifyArg(
            method = "<init>*",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;<init>(Lnet/minecraft/world/level/storage/WritableLevelData;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/core/Holder;Ljava/util/function/Supplier;ZZJI)V",
                    remap = true
            ),
            index = 4,
            require = 0
    )
    private static Supplier<ProfilerFiller> omnifix$rebindProfiler(
            Supplier<ProfilerFiller> profiler,
            @Local(argsOnly = true) Level level
    ) {
        if (level == null) {
            return profiler;
        }
        return level.getProfilerSupplier();
    }
}
