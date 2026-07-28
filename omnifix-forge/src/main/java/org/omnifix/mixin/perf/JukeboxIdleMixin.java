package org.omnifix.mixin.perf;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Root cause: jukeboxes with no disc still receive {@code playRecordTick} every game tick via the
 * block entity ticker. Skip when not playing and the disc slot is empty.
 */
@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxIdleMixin {

    @Shadow
    @Final
    private NonNullList<ItemStack> items;

    @Shadow
    public abstract boolean isRecordPlaying();

    @Inject(method = "playRecordTick", at = @At("HEAD"), cancellable = true)
    private static void omnifix$skipIdleJukebox(
            Level level,
            BlockPos pos,
            BlockState state,
            JukeboxBlockEntity jukebox,
            CallbackInfo ci
    ) {
        JukeboxIdleMixin self = (JukeboxIdleMixin) (Object) jukebox;
        if (!self.isRecordPlaying() && self.items.get(0).isEmpty()) {
            ci.cancel();
        }
    }
}
