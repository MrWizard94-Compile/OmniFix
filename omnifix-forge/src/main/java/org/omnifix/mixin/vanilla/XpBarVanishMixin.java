package org.omnifix.mixin.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * MC-79545 — Experience bar vanishes at extremely high levels.
 *
 * <p>Root cause: {@link Gui#renderExperienceBar} only draws the bar when
 * {@code getXpNeededForNextLevel() > 0}. The formula {@code 112 + (level - 30) * 9} overflows
 * signed {@code int} at high levels and becomes ≤0, so the bar is skipped while the level number
 * still renders. Clamp the value used for bar rendering to a positive range.
 */
@Mixin(Gui.class)
public abstract class XpBarVanishMixin {

    @ModifyExpressionValue(
            method = "renderExperienceBar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;getXpNeededForNextLevel()I"
            )
    )
    private int omnifix$clampXpNeededForBar(int xpNeeded) {
        return Mth.clamp(xpNeeded, 1, Integer.MAX_VALUE);
    }
}
