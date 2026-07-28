package org.omnifix.mixin.perf;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Force-closes world-join loading screens once the client level and local player are ready.
 *
 * <p>After joining (or changing) a world, {@link ReceivingLevelScreen}, {@link ProgressScreen}
 * ({@code connect.joining}), and {@link LevelLoadingScreen} can remain for an extra frame cycle
 * even though {@link ClientLevel} and the local player already exist. Resource-reload paths use
 * {@code LoadingOverlay} / dirt message screens and are intentionally not dismissed.
 */
@Mixin(Minecraft.class)
public abstract class ForceCloseLoadingScreenMixin {

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Shadow
    @Nullable
    public ClientLevel level;

    @Shadow
    @Nullable
    public Screen screen;

    @Shadow
    public abstract void setScreen(@Nullable Screen screen);

    @Inject(method = "tick", at = @At("HEAD"), require = 0)
    private void omnifix$forceCloseWorldLoadingScreen(CallbackInfo ci) {
        if (this.player == null || this.level == null) {
            return;
        }
        Screen current = this.screen;
        if (current instanceof ReceivingLevelScreen
                || current instanceof LevelLoadingScreen
                || current instanceof ProgressScreen) {
            this.setScreen(null);
        }
    }
}
