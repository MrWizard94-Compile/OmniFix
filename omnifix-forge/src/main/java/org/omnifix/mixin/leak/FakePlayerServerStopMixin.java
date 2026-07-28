package org.omnifix.mixin.leak;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * Clears Forge {@link FakePlayerFactory} static caches when the server stops so FakePlayers
 * (and any attached listeners) do not survive integrated-server restarts.
 *
 * <p>Root cause class: AllTheLeaks forge FakePlayer leak — factory maps hold players past
 * {@code stopServer}. Field layout is not part of Forge's public API, so discovery is reflective
 * and best-effort across 47.x builds.
 */
@Mixin(MinecraftServer.class)
public abstract class FakePlayerServerStopMixin {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Inject(method = "stopServer", at = @At("TAIL"))
    private void omnifix$clearFakePlayerFactory(CallbackInfo ci) {
        int cleared = 0;
        for (Field field : FakePlayerFactory.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !Map.class.isAssignableFrom(field.getType())) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object value = field.get(null);
                if (!(value instanceof Map<?, ?> map) || map.isEmpty()) {
                    continue;
                }
                if (clearFakePlayerMap(map)) {
                    cleared++;
                }
            } catch (Throwable t) {
                LOGGER.debug("[OmniFix] Could not clear FakePlayerFactory field {}", field.getName(), t);
            }
        }
        if (cleared > 0) {
            LOGGER.debug("[OmniFix] Cleared {} FakePlayerFactory map(s) on server stop.", cleared);
        }
    }

    private static boolean clearFakePlayerMap(Map<?, ?> map) {
        Object sample = map.values().iterator().next();
        if (sample instanceof FakePlayer) {
            map.clear();
            return true;
        }
        if (sample instanceof Map<?, ?> nested && !nested.isEmpty()
                && nested.values().iterator().next() instanceof FakePlayer) {
            for (Object v : map.values()) {
                if (v instanceof Map<?, ?> m) {
                    m.clear();
                }
            }
            map.clear();
            return true;
        }
        return false;
    }
}
