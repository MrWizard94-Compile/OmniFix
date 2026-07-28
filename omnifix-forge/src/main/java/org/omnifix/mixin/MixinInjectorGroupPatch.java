package org.omnifix.mixin;

import com.mojang.logging.LogUtils;
import org.omnifix.util.DummyList;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.injection.struct.InjectorGroupInfo;

import java.lang.reflect.Field;

/**
 * Replaces {@code InjectorGroupInfo.NO_GROUP.members} with a no-op list so every inject registration
 * does not pin members forever (Fabric Mixin PR #99 / ModernFix-class).
 */
public final class MixinInjectorGroupPatch {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean applied;

    private MixinInjectorGroupPatch() {}

    public static void apply() {
        if (applied) {
            return;
        }
        applied = true;
        try {
            Field groupMembersField = InjectorGroupInfo.class.getDeclaredField("members");
            groupMembersField.setAccessible(true);
            Field noGroupField = InjectorGroupInfo.Map.class.getDeclaredField("NO_GROUP");
            noGroupField.setAccessible(true);
            InjectorGroupInfo noGroup = (InjectorGroupInfo) noGroupField.get(null);
            groupMembersField.set(noGroup, new DummyList<>());
            LOGGER.info("[OmniFix] Patched Mixin InjectorGroupInfo.NO_GROUP member list");
        } catch (NoSuchFieldException ignored) {
            // Connector/newer Mixin may already ship the fix.
        } catch (RuntimeException | ReflectiveOperationException e) {
            LOGGER.error("[OmniFix] Failed to patch Mixin injector group leak", e);
        }
    }
}
