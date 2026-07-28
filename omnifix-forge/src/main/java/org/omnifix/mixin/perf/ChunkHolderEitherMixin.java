package org.omnifix.mixin.perf;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.chunk.LevelChunk;
import org.omnifix.util.EitherUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.CompletableFuture;

/**
 * Avoid Optional allocation on ticking/full chunk accessors.
 */
@Mixin(value = ChunkHolder.class, priority = 500)
public abstract class ChunkHolderEitherMixin {

    @Shadow
    public abstract CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> getTickingChunkFuture();

    @Shadow
    public abstract CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> getFullChunkFuture();

    /**
     * @author OmniFix (ModernFix-class)
     * @reason avoid Optional
     */
    @Overwrite
    public LevelChunk getTickingChunk() {
        Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> either =
                this.getTickingChunkFuture().getNow(null);
        return either == null ? null : EitherUtil.leftOrNull(either);
    }

    /**
     * @author OmniFix (ModernFix-class)
     * @reason avoid Optional
     */
    @Overwrite
    public LevelChunk getFullChunk() {
        Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> either =
                this.getFullChunkFuture().getNow(null);
        return either == null ? null : EitherUtil.leftOrNull(either);
    }
}
