package org.omnifix.mixin.feature;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.StartupMessageManager;
import net.minecraftforge.fml.event.IModBusEvent;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.RegisterEvent;
import org.omnifix.client.AsyncLoadingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Client: progress bar per registry + async splash while {@link GameData#postRegisterEvents()} runs.
 */
@Mixin(value = GameData.class, remap = false)
public abstract class GameDataRegistryProgressMixin {

    @Unique
    private static AsyncLoadingScreen omnifix$asyncScreen;

    @Inject(
            method = "postRegisterEvents",
            at = @At(value = "INVOKE", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;", ordinal = 0))
    private static void omnifix$createAsyncScreen(CallbackInfo ci) {
        try {
            omnifix$asyncScreen = new AsyncLoadingScreen();
        } catch (RuntimeException e) {
            // Headless / no window — progress bars still apply.
            omnifix$asyncScreen = null;
        }
    }

    @Inject(
            method = "postRegisterEvents",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/RuntimeException;getSuppressed()[Ljava/lang/Throwable;",
                    ordinal = 0))
    private static void omnifix$closeAsyncScreen(CallbackInfo ci) {
        if (omnifix$asyncScreen != null) {
            omnifix$asyncScreen.close();
            omnifix$asyncScreen = null;
        }
    }

    @Redirect(
            method = "postRegisterEvents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/fml/ModLoader;postEventWrapContainerInModOrder(Lnet/minecraftforge/eventbus/api/Event;)V"))
    private static <T extends Event & IModBusEvent> void omnifix$postWithProgress(ModLoader loader, T event) {
        RegisterEvent registryEvent = (RegisterEvent) event;
        var pb = StartupMessageManager.addProgressBar(
                registryEvent.getRegistryKey().location().toString(), ModList.get().size());
        try {
            loader.postEventWithWrapInModOrder(
                    event,
                    (mc, e) -> {
                        ModLoadingContext.get().setActiveContainer(mc);
                        pb.label(pb.name() + " - " + mc.getModInfo().getDisplayName());
                        pb.increment();
                    },
                    (mc, e) -> ModLoadingContext.get().setActiveContainer(null));
        } finally {
            pb.complete();
        }
    }
}
