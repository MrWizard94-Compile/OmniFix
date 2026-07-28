package org.omnifix.mixin.perf;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.omnifix.registry.DelegateHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;
import java.util.Map;

/**
 * Faster ForgeRegistry.getDelegateOrThrow: OpenHashMap backing + hot path on Block/Item DelegateHolder.
 */
@Mixin(value = ForgeRegistry.class, remap = false)
public abstract class ForgeRegistryDelegateMixin<V> {

    @Shadow
    @Final
    private Map<ResourceLocation, Holder.Reference<V>> delegatesByName = new Object2ObjectOpenHashMap<>();

    @Shadow
    @Final
    private Map<V, Holder.Reference<V>> delegatesByValue =
            new Object2ObjectOpenHashMap<>(Hash.DEFAULT_INITIAL_SIZE, 0.5F);

    @Shadow
    public abstract ResourceKey<Registry<V>> getRegistryKey();

    @Shadow
    @Final
    private RegistryManager stage;

    /**
     * @author OmniFix
     * @reason avoid Optional/allocating wrappers on every lookup
     */
    @Overwrite
    public Holder.Reference<V> getDelegateOrThrow(ResourceLocation location) {
        Holder.Reference<V> holder = delegatesByName.get(location);
        if (holder == null) {
            throw new IllegalArgumentException(
                    String.format(Locale.ENGLISH, "No delegate exists for location %s", location));
        }
        return holder;
    }

    /**
     * @author OmniFix
     */
    @Overwrite
    public Holder.Reference<V> getDelegateOrThrow(ResourceKey<V> rkey) {
        Holder.Reference<V> holder = delegatesByName.get(rkey.location());
        if (holder == null) {
            throw new IllegalArgumentException(
                    String.format(Locale.ENGLISH, "No delegate exists for key %s", rkey));
        }
        return holder;
    }

    @Inject(method = "bindDelegate", at = @At("RETURN"))
    private void omnifix$attachDelegate(
            ResourceKey<V> rkey,
            V value,
            CallbackInfoReturnable<Holder.Reference<V>> cir
    ) {
        if (this.stage == RegistryManager.ACTIVE && value instanceof DelegateHolder<?>) {
            @SuppressWarnings("unchecked")
            DelegateHolder<V> dh = (DelegateHolder<V>) value;
            dh.omnifix$setDelegate(this.getRegistryKey(), cir.getReturnValue());
        }
    }

    /**
     * @author OmniFix
     * @reason skip map lookup when Block/Item carries bound delegate
     */
    @Overwrite
    public Holder.Reference<V> getDelegateOrThrow(V value) {
        Holder.Reference<V> holder = null;
        if (this.stage == RegistryManager.ACTIVE && value instanceof DelegateHolder<?>) {
            @SuppressWarnings("unchecked")
            DelegateHolder<V> dh = (DelegateHolder<V>) value;
            holder = dh.omnifix$getDelegate(this.getRegistryKey());
        }
        if (holder == null) {
            holder = delegatesByValue.get(value);
            if (holder == null) {
                throw new IllegalArgumentException(
                        String.format(Locale.ENGLISH, "No delegate exists for value %s", value));
            }
        }
        return holder;
    }
}
