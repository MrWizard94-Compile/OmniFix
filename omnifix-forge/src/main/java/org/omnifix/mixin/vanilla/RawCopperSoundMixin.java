package org.omnifix.mixin.vanilla;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MC-223153 — Block of Raw Copper uses the default stone sound type instead of copper.
 *
 * <p>Still present in 1.20.1 official: {@code Blocks.RAW_COPPER_BLOCK} is registered with
 * {@code Properties.of()...strength(...)} and no {@code .sound(SoundType.COPPER)} call, so it
 * inherits the default {@link SoundType#STONE}. Override {@link Block#getSoundType(BlockState)}
 * for that block only.
 */
@Mixin(Block.class)
public abstract class RawCopperSoundMixin {

    @Inject(
            method = "getSoundType(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/SoundType;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void omnifix$rawCopperUsesCopperSounds(BlockState state, CallbackInfoReturnable<SoundType> cir) {
        if ((Object) this == Blocks.RAW_COPPER_BLOCK) {
            cir.setReturnValue(SoundType.COPPER);
        }
    }
}
