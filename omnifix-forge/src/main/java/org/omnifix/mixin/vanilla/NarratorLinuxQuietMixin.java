package org.omnifix.mixin.vanilla;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.logging.LogUtils;
import com.mojang.text2speech.Narrator;
import com.mojang.text2speech.NarratorLinux;
import com.mojang.text2speech.OperatingSystem;
import net.minecraft.client.GameNarrator;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Linux flite narrator init often fails without libflite and dumps a huge stack.
 * Prefer a quiet EMPTY narrator on Linux init failure.
 */
@Mixin(GameNarrator.class)
public abstract class NarratorLinuxQuietMixin {

    private static final Logger LOGGER = LogUtils.getLogger();

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/text2speech/Narrator;getNarrator()Lcom/mojang/text2speech/Narrator;"
            )
    )
    private Narrator omnifix$quietLinuxInit(Operation<Narrator> original) {
        try {
            if (OperatingSystem.get() == OperatingSystem.LINUX) {
                return new NarratorLinux();
            }
            return original.call();
        } catch (Narrator.InitializeException e) {
            LOGGER.warn("[OmniFix] Narrator init failed ({}). Using empty narrator.", e.getMessage());
            return Narrator.EMPTY;
        } catch (Throwable t) {
            LOGGER.warn("[OmniFix] Narrator init failed: {}", t.toString());
            return Narrator.EMPTY;
        }
    }
}
