package org.omnifix.mixin.perf;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ProfiledReloadInstance;
import org.omnifix.util.NamedPreparableResourceListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Name reload listeners with FQCN and sort profile output by cost (most expensive first).
 */
@Mixin(ProfiledReloadInstance.class)
public abstract class ProfiledReloadInstanceMixin {

    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static List<PreparableReloadListener> omnifix$getWrappedListeners(
            List<PreparableReloadListener> listeners
    ) {
        List<PreparableReloadListener> newList = new ArrayList<>(listeners.size());
        for (PreparableReloadListener listener : listeners) {
            String className = listener.getClass().getName();
            if (className.startsWith("net.minecraftforge.")
                    || className.startsWith("net.neoforged.")
                    || className.startsWith("net.fabricmc.")) {
                newList.add(listener);
            } else {
                newList.add(new NamedPreparableResourceListener(listener));
            }
        }
        return newList;
    }

    @ModifyVariable(method = "finish", ordinal = 0, argsOnly = true, at = @At("HEAD"))
    private List<ProfiledReloadInstance.State> omnifix$sortStates(
            List<ProfiledReloadInstance.State> datapoints
    ) {
        datapoints = new ArrayList<>(datapoints);
        datapoints.sort(Comparator
                .<ProfiledReloadInstance.State>comparingLong(
                        s -> s.preparationNanos.get() + s.reloadNanos.get())
                .reversed());
        return datapoints;
    }
}
