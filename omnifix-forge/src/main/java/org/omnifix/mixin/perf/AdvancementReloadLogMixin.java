package org.omnifix.mixin.perf;

import net.minecraft.server.ServerAdvancementManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Advancement datapack apply logs full exception stacks for every bad advancement, drowning useful
 * errors. Keep the location and a short message only (same approach as
 * {@link RecipeManagerReloadLogMixin}).
 *
 * <p>On 1.20.1 the parse failure is logged from the {@code apply} forEach body
 * ({@code lambda$apply$0} / SRG {@code m_278533_}); {@code apply} is soft-matched if the call is
 * ever inlined.
 */
@Mixin(value = ServerAdvancementManager.class, priority = 2000)
public abstract class AdvancementReloadLogMixin {

    @Redirect(
            method = {
                    "lambda$apply$0(Ljava/util/Map;Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonElement;)V",
                    "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
                    remap = false
            ),
            require = 0
    )
    private void omnifix$compactAdvancementError(Logger logger, String format, Object location, Object exc) {
        // Never pass a Throwable through SLF4J's multi-arg error (Logback prints its stack).
        // Advancement format already has two placeholders (location + message/exception).
        logger.error(format, location, String.valueOf(exc));
    }
}
