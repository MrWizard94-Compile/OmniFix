package org.omnifix.forge;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import com.valkyrienportals.ValkyrienPortalsCompatBootstrap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.omnifix.client.OmniFixClientEvents;
import org.omnifix.compat.createportals.IpPortalTrackCompat;
import org.omnifix.config.ConfigFixer;
import org.omnifix.config.NightConfigFixer;
import org.omnifix.config.NightConfigWatchThrottler;
import org.omnifix.duck.IProfilingServerFunctionManager;
import org.omnifix.entity.AttributeSupplierLaunchGate;
import org.omnifix.kernel.OmniFixConstants;
import org.omnifix.kernel.StackDomain;
import org.omnifix.kernel.StackPolicyEngine;
import org.omnifix.kernel.StackProfile;
import org.omnifix.kernel.feature.FeatureUnit;
import org.omnifix.kernel.feature.FeatureUnitRegistry;
import org.omnifix.kernel.feature.FeatureUnits;
import org.omnifix.load.ModFileScanDataCompactor;
import org.omnifix.mixin.ClassInfoManager;
import org.omnifix.mixin.leak.ClientLevelLeaveHandler;
import org.omnifix.mixin.leak.CloneLeakHandlers;
import org.omnifix.mixin.leak.ServerLeakHandlers;
import org.omnifix.util.SafeRun;
import org.slf4j.Logger;

@Mod(OmniFixConstants.MOD_ID)
public final class OmniFix {

    private static final Logger LOGGER = LogUtils.getLogger();

    public OmniFix() {
        FeatureUnits.registerBuiltins();
        FeatureUnitRegistry.loadConfig(FMLPaths.CONFIGDIR.get().resolve("omnifix-features.properties"));

        registerStackProbes();
        StackPolicyEngine.resolve();
        ValkyrienPortalsCompatBootstrap.init();
        CloneLeakHandlers.register();
        // Server-side optional-mod leak handlers (Create Addition / Ars / Citadel / Alex's / Seasons / …).
        ServerLeakHandlers.register();
        // Client-only optional-mod leak handlers (GeckoLib / JEI / Flywheel / TF / Mowzie's / …).
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientLevelLeaveHandler::register);
        // Client timing, MemoryReserve, integrated watchdog, client config reload command.
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> OmniFixClientEvents::register);
        registerCreatePortalTracks();
        applyRuntimePatches();
        applyStdoutLogMirror();
        registerConfigReloadCommands();
        registerDiagnosticCommands();
        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLCommonSetupEvent event) ->
                AttributeSupplierLaunchGate.markLaunchComplete());
        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLLoadCompleteEvent event) ->
                event.enqueueWork(ClassInfoManager::clearIfEnabled));
        logActiveFeatureUnits();

        LOGGER.info("[OmniFix] Profile: {}", StackPolicyEngine.getProfile());
        if (StackPolicyEngine.getProfile() == StackProfile.HEAVY_PHYSICS_PORTAL) {
            LOGGER.info("[OmniFix] Heavy physics + portal stack detected.");
        }
    }

    /**
     * Runtime (non-mixin) patches that must run after ModList is ready. Skipped when ModernFix is
     * present so peer patches do not double-apply.
     */
    private static void applyRuntimePatches() {
        if (modPresent("modernfix")) {
            LOGGER.info("[OmniFix] modernfix present — skipping NightConfig / scan-compact runtime patches");
            return;
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.PERF_NIGHTCONFIG_WATCH)) {
            SafeRun.run(NightConfigWatchThrottler::throttle, "NightConfig watch throttle");
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.BUGFIX_NIGHTCONFIG_CRASH)) {
            NightConfigFixer.monitorFileWatcher();
            ConfigFixer.replaceConfigHandlers();
        }
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.PERF_MOD_SCAN_COMPACT)) {
            SafeRun.run(ModFileScanDataCompactor::compact, "ModFileScanData compact");
        }
    }

    /**
     * Optional: mirror System.out/err into log4j so launch console spam is captured in logs.
     * Default off ({@link FeatureUnits#FEATURE_STDOUT_LOG}).
     */
    private static void applyStdoutLogMirror() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.FEATURE_STDOUT_LOG)) {
            return;
        }
        if (modPresent("modernfix")) {
            return;
        }
        SafeRun.run(() -> {
            org.omnifix.util.TracingPrintStream.install();
        }, "install stdout/stderr log mirror");
    }

    private static void registerConfigReloadCommands() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.BUGFIX_NIGHTCONFIG_CRASH)) {
            return;
        }
        if (modPresent("modernfix")) {
            return;
        }
        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
            for (String name : new String[] {"ofsrc", "omnifix_config_reload"}) {
                event.getDispatcher().register(LiteralArgumentBuilder.<CommandSourceStack>literal(name)
                        .requires(source -> source.hasPermission(3))
                        .executes(context -> {
                            NightConfigFixer.runReloads();
                            return 1;
                        }));
            }
        });
    }

    private static void registerDiagnosticCommands() {
        if (!FeatureUnitRegistry.isConfigEnabled(FeatureUnits.FEATURE_MCFUNCTION_PROFILING)) {
            return;
        }
        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
            event.getDispatcher().register(LiteralArgumentBuilder.<CommandSourceStack>literal("omnifix")
                    .requires(source -> source.hasPermission(3))
                    .then(LiteralArgumentBuilder.<CommandSourceStack>literal("mcfunctions")
                            .executes(context -> {
                                var functions = context.getSource().getServer().getFunctions();
                                if (functions instanceof IProfilingServerFunctionManager profiler) {
                                    String results = profiler.omnifix$getProfilingResults();
                                    if (results.isEmpty()) {
                                        context.getSource().sendSuccess(
                                                () -> Component.literal("No #minecraft:tick function timings yet."),
                                                false);
                                    } else {
                                        context.getSource().sendSuccess(
                                                () -> Component.literal("mcfunction tick breakdown:\n" + results),
                                                false);
                                    }
                                    return 1;
                                }
                                context.getSource().sendFailure(
                                        Component.literal("mcfunction profiling mixin not active."));
                                return 0;
                            })));
        });
    }

    /**
     * Create × Immersive Portals track pairing. Deferred to load-complete so the override registers
     * after Create's common-setup defaults; the listener (and with it the compat class, which imports
     * Create and IP types) is only hooked up when both mods are present and the FeatureUnit is on.
     */
    private static void registerCreatePortalTracks() {
        if (!FeatureUnitRegistry.isActive(FeatureUnits.CREATE_IP_TRACKS_A)) {
            return;
        }
        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLLoadCompleteEvent event) ->
                event.enqueueWork(IpPortalTrackCompat::register));
    }

    private static void registerStackProbes() {
        StackPolicyEngine.registerProbe(StackDomain.VALKYRIEN_SKIES,
                () -> modPresent("valkyrienskies"));
        StackPolicyEngine.registerProbe(StackDomain.IMMERSIVE_PORTALS,
                () -> modPresent("immersive_portals"));
        StackPolicyEngine.registerProbe(StackDomain.EMBEDDUM,
                () -> modPresent("embeddium") || modPresent("rubidium"));
        StackPolicyEngine.registerProbe(StackDomain.CREATE,
                () -> modPresent("create"));
        StackPolicyEngine.registerProbe(StackDomain.OCULUS,
                () -> modPresent("oculus") || modPresent("iris"));
        StackPolicyEngine.registerProbe(StackDomain.FERRITECORE,
                () -> modPresent("ferritecore"));
        StackPolicyEngine.registerProbe(StackDomain.RADIUM,
                () -> modPresent("radium") || modPresent("canary"));
    }

    private static void logActiveFeatureUnits() {
        int active = 0;
        for (FeatureUnit unit : FeatureUnitRegistry.all()) {
            if (FeatureUnitRegistry.isActive(unit.id())) {
                active++;
                LOGGER.info("[OmniFix] FeatureUnit active: {} — {}", unit.id(), unit.displayName());
            }
        }
        LOGGER.info("[OmniFix] {} FeatureUnit(s) active ({} registered).", active, FeatureUnitRegistry.all().size());
    }

    private static boolean modPresent(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
