package org.omnifix.mixin.perf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Root cause: empty beehives still run {@code serverTick} every game tick (occupants walk, ambient
 * sound roll, debug hive packets) even with zero stored bees. Skip when {@code stored} is empty.
 */
@Mixin(BeehiveBlockEntity.class)
public abstract class BeehiveEmptyMixin {

    @Shadow
    @Final
    private List<?> stored;

    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private static void omnifix$skipEmptyHive(
            Level level,
            BlockPos pos,
            BlockState state,
            BeehiveBlockEntity hive,
            CallbackInfo ci
    ) {
        BeehiveEmptyMixin self = (BeehiveEmptyMixin) (Object) hive;
        if (self.stored.isEmpty()) {
            ci.cancel();
        }
    }
}
