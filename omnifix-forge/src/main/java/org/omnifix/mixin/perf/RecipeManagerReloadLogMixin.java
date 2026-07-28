package org.omnifix.mixin.perf;

import net.minecraft.world.item.crafting.RecipeManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Recipe datapack apply logs full exception stacks for every bad recipe, drowning useful errors.
 * Keep the location and a short message only.
 */
@Mixin(value = RecipeManager.class, priority = 2000)
public abstract class RecipeManagerReloadLogMixin {

    @Redirect(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
                    remap = false
            ),
            require = 0
    )
    private void omnifix$compactRecipeError(Logger logger, String format, Object location, Object exc) {
        logger.error(format + ": {}", location, String.valueOf(exc));
    }
}
