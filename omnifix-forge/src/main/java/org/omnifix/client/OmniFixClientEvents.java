package org.omnifix.client;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.omnifix.config.NightConfigFixer;
import org.omnifix.kernel.feature.FeatureUnitRegistry;
import org.omnifix.kernel.feature.FeatureUnits;
import org.omnifix.world.IntegratedWatchdog;

/**
 * Client-side event hooks for timing + integrated server watchdog + config reload command.
 */
public final class OmniFixClientEvents {

    private OmniFixClientEvents() {}

    public static void register() {
        if (!FMLEnvironment.dist.isClient()) {
            return;
        }
        MinecraftForge.EVENT_BUS.register(new OmniFixClientEvents());
        // Free vanilla's unused memory reserve early on client.
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.PERF_MEMORY_RESERVE)) {
            net.minecraft.util.MemoryReserve.release();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onClientCommands(RegisterClientCommandsEvent event) {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.BUGFIX_NIGHTCONFIG_CRASH)) {
            return;
        }
        if (ModList.get().isLoaded("modernfix")) {
            return;
        }
        event.getDispatcher().register(LiteralArgumentBuilder.<CommandSourceStack>literal("ofc")
                .executes(context -> {
                    NightConfigFixer.runReloads();
                    return 1;
                }));
        event.getDispatcher().register(LiteralArgumentBuilder.<CommandSourceStack>literal("omnifix_config_reload")
                .executes(context -> {
                    NightConfigFixer.runReloads();
                    return 1;
                }));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderTickEnd(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            OmniFixClientTiming.onRenderTickEnd();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRecipes(RecipesUpdatedEvent e) {
        OmniFixClientTiming.onRecipesUpdated();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTags(TagsUpdatedEvent e) {
        OmniFixClientTiming.onTagsUpdated();
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.PERF_INTEGRATED_WATCHDOG)) {
            return;
        }
        if (!event.getServer().isDedicatedServer()) {
            IntegratedWatchdog watchdog = new IntegratedWatchdog(event.getServer());
            watchdog.start();
        }
    }
}
