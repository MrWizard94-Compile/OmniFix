package org.omnifix.config;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.IConfigEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Serializes each mod's {@link IConfigEvent} handler so concurrent NightConfig watcher + Forge
 * main-thread posts cannot re-enter the same mod's config logic and crash.
 */
public final class ConfigFixer {

    private static final Logger LOGGER = LogUtils.getLogger();

    private ConfigFixer() {}

    public static void replaceConfigHandlers() {
        ModList.get().forEachModContainer((id, container) -> {
            try {
                Optional<Consumer<IConfigEvent>> configOpt =
                        ObfuscationReflectionHelper.getPrivateValue(ModContainer.class, container, "configHandler");
                if (configOpt != null && configOpt.isPresent()) {
                    ObfuscationReflectionHelper.setPrivateValue(
                            ModContainer.class,
                            container,
                            Optional.of(new LockingConfigHandler(id, configOpt.get())),
                            "configHandler");
                }
            } catch (RuntimeException e) {
                LOGGER.error("[OmniFix] Error replacing config handler for {}", id, e);
            }
        });
        LOGGER.info("[OmniFix] Wrapped mod config handlers with per-mod locks");
    }

    private static final class LockingConfigHandler implements Consumer<IConfigEvent> {
        private final Consumer<IConfigEvent> actualHandler;
        private final String modId;
        private final Lock lock = new ReentrantLock();

        LockingConfigHandler(String id, Consumer<IConfigEvent> actualHandler) {
            this.modId = id;
            this.actualHandler = actualHandler;
        }

        @Override
        public void accept(IConfigEvent event) {
            try {
                if (lock.tryLock(2, TimeUnit.SECONDS)) {
                    try {
                        this.actualHandler.accept(event);
                    } finally {
                        lock.unlock();
                    }
                } else {
                    LOGGER.error(
                            "[OmniFix] Failed to post config event for {}, another thread holds the lock",
                            modId);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public String toString() {
            return "OmniFixLockingConfigHandler{id=" + modId + "}";
        }
    }
}
