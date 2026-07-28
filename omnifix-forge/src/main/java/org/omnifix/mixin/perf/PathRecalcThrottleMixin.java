package org.omnifix.mixin.perf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Root cause: {@link PathNavigation} re-runs A* pathfinding far more often than needed.
 * Goals and delayed recomputation call {@link PathNavigation#createPath} /
 * {@link PathNavigation#recomputePath} even when the mob has not changed block position and the
 * navigation target {@link BlockPos} is unchanged. Vanilla already reuses the current path only
 * when the requested set contains {@code targetPos} (which is the last path end, not necessarily
 * the requested destination), and {@code recomputePath} nulls the path before rebuilding — so
 * stationary pathing mobs still dominate AI CPU on busy servers.
 *
 * <p>Fix: per-navigation throttle. If the current path is still present and not done, the mob's
 * block position matches the last successful compute, the target matches the last compute, and
 * fewer than {@link #OMNIFIX$THROTTLE_TICKS} game ticks have elapsed, skip pathfinding and reuse
 * the existing path. Any block-position change (push / walk) or target change invalidates the
 * throttle immediately. Server-side only.
 *
 * <p>Default-safe: never skips when {@code path == null} or {@code path.isDone()}. Does not alter
 * follow-the-path / stuck detection; only gates recomputation of the path object.
 */
@Mixin(PathNavigation.class)
public abstract class PathRecalcThrottleMixin {

    /**
     * Window during which identical (mob block pos, target) may reuse the last path.
     * Matches vanilla {@code MAX_TIME_RECOMPUTE} (20) so a stationary recompute at the vanilla
     * cadence is skipped once when nothing changed; a later request after the window still
     * refreshes for world edits.
     */
    @Unique
    private static final int OMNIFIX$THROTTLE_TICKS = 20;

    @Shadow
    @Final
    protected Mob mob;

    @Shadow
    @Final
    protected Level level;

    @Shadow
    @Nullable
    protected Path path;

    @Shadow
    protected boolean hasDelayedRecomputation;

    @Shadow
    @Nullable
    private BlockPos targetPos;

    /** Packed {@link BlockPos} of the mob at last successful path compute. */
    @Unique
    private long omnifix$lastMobBlockPos = Long.MIN_VALUE;

    /** Packed requested / active target at last successful path compute. */
    @Unique
    private long omnifix$lastTargetBlockPos = Long.MIN_VALUE;

    /** {@link Level#getGameTime()} when last successful path compute was recorded. */
    @Unique
    private long omnifix$lastComputeGameTime = Long.MIN_VALUE;

    /**
     * Whether it is safe to skip pathfinding and keep {@link #path}.
     *
     * @param requestedTarget destination under consideration; {@code null} rejects reuse
     */
    @Unique
    private boolean omnifix$canReusePath(@Nullable BlockPos requestedTarget) {
        if (this.level.isClientSide) {
            return false;
        }
        if (this.path == null || this.path.isDone()) {
            return false;
        }
        if (requestedTarget == null) {
            return false;
        }
        long gameTime = this.level.getGameTime();
        // Allow recompute once the throttle window has fully elapsed (world may have changed).
        if (gameTime - this.omnifix$lastComputeGameTime > (long) OMNIFIX$THROTTLE_TICKS) {
            return false;
        }
        // Entity pushed / walked into a new block → must repath.
        if (this.mob.blockPosition().asLong() != this.omnifix$lastMobBlockPos) {
            return false;
        }
        // Target moved (or a different destination was requested) → must repath.
        if (requestedTarget.asLong() != this.omnifix$lastTargetBlockPos) {
            return false;
        }
        return true;
    }

    /**
     * Record mob block pos + target after a real path result so later identical requests can reuse.
     */
    @Unique
    private void omnifix$rememberCompute(@Nullable BlockPos requestedTarget) {
        if (requestedTarget == null) {
            return;
        }
        this.omnifix$lastMobBlockPos = this.mob.blockPosition().asLong();
        this.omnifix$lastTargetBlockPos = requestedTarget.asLong();
        this.omnifix$lastComputeGameTime = this.level.getGameTime();
    }

    /**
     * Pick a single throttle key from the createPath destination set.
     * Single-element sets use that element; multi-target sets only throttle when they still
     * contain the last recorded target (or vanilla {@code targetPos}).
     */
    @Unique
    @Nullable
    private BlockPos omnifix$resolveRequestedTarget(Set<BlockPos> positions) {
        if (positions == null || positions.isEmpty()) {
            return null;
        }
        if (positions.size() == 1) {
            return positions.iterator().next();
        }
        if (this.omnifix$lastTargetBlockPos != Long.MIN_VALUE) {
            for (BlockPos pos : positions) {
                if (pos != null && pos.asLong() == this.omnifix$lastTargetBlockPos) {
                    return pos;
                }
            }
        }
        if (this.targetPos != null && positions.contains(this.targetPos)) {
            return this.targetPos;
        }
        return null;
    }

    /**
     * Skip {@link PathNavigation#recomputePath} when nothing relevant changed.
     * Clears delayed recompute so {@code tick} does not spin on a perpetual flag while we hold
     * a still-valid path.
     */
    @Inject(method = "recomputePath", at = @At("HEAD"), cancellable = true)
    private void omnifix$throttleRecomputePath(CallbackInfo ci) {
        BlockPos target = this.targetPos;
        if (omnifix$canReusePath(target)) {
            this.hasDelayedRecomputation = false;
            ci.cancel();
        }
    }

    /**
     * Hot path: all public {@code createPath} overloads funnel into this protected method.
     * Reuse the existing path when mob block pos + target are unchanged within the throttle window.
     */
    @Inject(
            method = "createPath(Ljava/util/Set;IZIF)Lnet/minecraft/world/level/pathfinder/Path;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void omnifix$throttleCreatePath(
            Set<BlockPos> positions,
            int regionOffset,
            boolean offsetUpward,
            int reachRange,
            float maxRange,
            CallbackInfoReturnable<Path> cir
    ) {
        BlockPos requested = omnifix$resolveRequestedTarget(positions);
        if (omnifix$canReusePath(requested)) {
            cir.setReturnValue(this.path);
        }
    }

    /**
     * After a successful pathfind (or vanilla early reuse), remember mob/target for the throttle.
     * Null / done results do not refresh the window so the next request pathfinds normally.
     */
    @Inject(
            method = "createPath(Ljava/util/Set;IZIF)Lnet/minecraft/world/level/pathfinder/Path;",
            at = @At("RETURN")
    )
    private void omnifix$rememberCreatePath(
            Set<BlockPos> positions,
            int regionOffset,
            boolean offsetUpward,
            int reachRange,
            float maxRange,
            CallbackInfoReturnable<Path> cir
    ) {
        if (this.level.isClientSide) {
            return;
        }
        Path result = cir.getReturnValue();
        if (result == null || result.isDone()) {
            return;
        }
        BlockPos requested = omnifix$resolveRequestedTarget(positions);
        if (requested == null && this.targetPos != null) {
            requested = this.targetPos;
        }
        omnifix$rememberCompute(requested);
    }
}
