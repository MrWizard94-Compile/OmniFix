package org.omnifix.mixin.bugfix.world_leaks;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Mitigates memory retained by leaked {@link ClientLevel} instances by clearing chunk storage,
 * replacing the light engine, and draining block-entity tickers before the level field is nulled.
 * Fields are widened via access transformers.
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Shadow
    @Nullable
    public ClientLevel level;

    /**
     * To mitigate the effect of leaked client worlds, clear most of the data structures that waste memory.
     */
    @Inject(
            method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.PUTFIELD,
                    target = "Lnet/minecraft/client/Minecraft;level:Lnet/minecraft/client/multiplayer/ClientLevel;"))
    private void omnifix$clearLevelDataForLeaks(CallbackInfo ci) {
        if (this.level != null) {
            try {
                AtomicReferenceArray<LevelChunk> chunks = this.level.getChunkSource().storage.chunks;
                for (int i = 0; i < chunks.length(); i++) {
                    chunks.set(i, null);
                }
                this.level.getChunkSource().lightEngine =
                        new LevelLightEngine(this.level.getChunkSource(), false, false);
                // clear BE list otherwise they will hold chunks
                this.level.blockEntityTickers.clear();
            } catch (RuntimeException e) {
                LOGGER.error("Exception clearing level data", e);
            }
        }
    }
}
