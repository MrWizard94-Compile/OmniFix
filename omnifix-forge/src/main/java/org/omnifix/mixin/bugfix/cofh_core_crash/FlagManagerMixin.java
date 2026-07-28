package org.omnifix.mixin.bugfix.cofh_core_crash;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * CoFH Core {@code FlagManager} accesses the static {@code FLAGS} map without synchronization.
 * Concurrent {@code getOrCreateFlag} from resource reload / recipe threads crashes.
 *
 * <p>ModernFix-class port: wrap all {@code getOrCreateFlag} calls in a synchronized block on FLAGS.
 * Soft-loads the MethodHandle so class init never fails when CoFH is absent (mixin gated off).
 */
@Pseudo
@Mixin(targets = "cofh.lib.util.flags.FlagManager", remap = false)
public abstract class FlagManagerMixin {

    @Shadow
    @Final
    private static Object2ObjectOpenHashMap<String, ?> FLAGS;

    @Unique
    private static volatile MethodHandle omnifix$getOrCreateFlag;

    @Unique
    private static MethodHandle omnifix$handle() {
        MethodHandle h = omnifix$getOrCreateFlag;
        if (h != null) {
            return h;
        }
        synchronized (FlagManagerMixin.class) {
            if (omnifix$getOrCreateFlag == null) {
                try {
                    Class<?> target = Class.forName("cofh.lib.util.flags.FlagManager");
                    Method m = target.getDeclaredMethod("getOrCreateFlag", String.class);
                    m.setAccessible(true);
                    omnifix$getOrCreateFlag = MethodHandles.lookup().unreflect(m);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("CoFH FlagManager.getOrCreateFlag not found", e);
                }
            }
            return omnifix$getOrCreateFlag;
        }
    }

    @Redirect(method = "*", at = @At(value = "INVOKE", target = "getOrCreateFlag"), require = 0)
    @Coerce
    private Object omnifix$syncGetOrCreateFlag(@Coerce Object flagHandler, String flag) {
        if (flagHandler != this) {
            throw new AssertionError("Redirect targeted unexpected getOrCreateFlag invocation");
        }
        synchronized (FLAGS) {
            try {
                return omnifix$handle().invoke((Object) this, flag);
            } catch (Throwable e) {
                if (e instanceof RuntimeException re) {
                    throw re;
                }
                throw new RuntimeException(e);
            }
        }
    }
}
