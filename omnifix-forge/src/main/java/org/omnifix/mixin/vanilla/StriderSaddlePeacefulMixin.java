package org.omnifix.mixin.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * MC-232869 — Adult striders can spawn with saddles in Peaceful difficulty.
 *
 * <p>Root cause: {@link Strider#finalizeSpawn} rolls {@code nextInt(30) == 0} for a zombified-piglin
 * jockey and always calls {@code equipSaddle} on that path. In Peaceful the piglin despawns (or never
 * persists), leaving a free saddled strider. Force the first {@code nextInt} roll away from 0 on
 * Peaceful so the saddle jockey branch never runs.
 */
@Mixin(Strider.class)
public abstract class StriderSaddlePeacefulMixin extends Animal {

    protected StriderSaddlePeacefulMixin(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    @ModifyExpressionValue(
            method = "finalizeSpawn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/RandomSource;nextInt(I)I",
                    ordinal = 0
            )
    )
    private int omnifix$noPeacefulSaddleJockey(int random) {
        return this.level().getDifficulty() == Difficulty.PEACEFUL ? 1 : random;
    }
}
