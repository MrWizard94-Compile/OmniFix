package org.omnifix.mixin.perf;

import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

/**
 * Root cause: {@link GoalSelector} fully re-evaluates available goals every {@code newGoalRate}
 * ticks (vanilla default 3) while {@link GoalSelector#tickRunningGoals} still runs every tick.
 * Raising the rate modestly cuts goal-selection CPU on servers with many pathing mobs
 * (AI Improvements-class trade-off: new goals may start up to ~2 ticks later).
 */
@Mixin(GoalSelector.class)
public abstract class GoalSelectorRateMixin {

    private static final int OMNIFIX$NEW_GOAL_RATE = 5;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void omnifix$raiseNewGoalRate(Supplier<?> profiler, CallbackInfo ci) {
        ((GoalSelector) (Object) this).setNewGoalRate(OMNIFIX$NEW_GOAL_RATE);
    }
}
