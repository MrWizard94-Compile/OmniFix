package org.omnifix.mixin.perf;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Root cause: {@link ServerLevel#tickChunk} still walks every {@link LevelChunkSection} when
 * {@code randomTickSpeed > 0}, calling {@link LevelChunkSection#isRandomlyTicking()} per section
 * before any random position sampling. Chunks with no randomly-ticking blocks or fluids pay that
 * walk every server tick for every entity-ticking chunk (Lithium-class issue; independent
 * reimplementation, not a GPL copy).
 *
 * <p>Fix: when no section reports {@link LevelChunkSection#isRandomlyTicking()}, force the
 * {@code randomTickSpeed} parameter to {@code 0}. Vanilla already gates the entire random-tick
 * section loop on {@code randomTickSpeed > 0}, so the section walk and per-position sampling are
 * skipped. Thunder and ice/snow precipitation in the same method are preserved (a full
 * {@code HEAD} cancel of {@code tickChunk} would incorrectly drop those).
 *
 * <p>Mojmap 1.20.1: {@code ServerLevel.tickChunk(LevelChunk, int)},
 * {@code LevelChunk.getSections()}, {@code LevelChunkSection.isRandomlyTicking()}.
 */
@Mixin(ServerLevel.class)
public abstract class SkipEmptyRandomTickMixin {

    /**
     * Zero random-tick speed for chunks whose sections have no random-ticking blocks/fluids.
     *
     * @param randomTickSpeed game-rule random tick speed for this chunk tick
     * @param chunk           chunk being ticked (captured method arg)
     * @return original speed if any section needs random ticks; otherwise {@code 0}
     */
    @ModifyVariable(method = "tickChunk", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int omnifix$skipEmptyRandomTicks(int randomTickSpeed, LevelChunk chunk) {
        if (randomTickSpeed <= 0) {
            return 0;
        }
        LevelChunkSection[] sections = chunk.getSections();
        for (LevelChunkSection section : sections) {
            // Non-null guard: section arrays are dense in 1.20.1, but keep safe for edge loaders.
            if (section != null && section.isRandomlyTicking()) {
                return randomTickSpeed;
            }
        }
        return 0;
    }
}
