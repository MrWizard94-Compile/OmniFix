package org.omnifix.mixin.perf.model;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Function;

/**
 * Avoid stream allocation and re-resolve the same model location multiple times.
 */
@Mixin(value = MultiVariant.class, priority = 700)
public abstract class MultiVariantResolveMixin {

    @Shadow
    public abstract List<Variant> getVariants();

    /**
     * @author OmniFix (ModernFix-class)
     * @reason stream-free parent resolve with de-dup
     */
    @Overwrite
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter) {
        List<Variant> variants = this.getVariants();
        int size = variants.size();
        if (size == 1) {
            modelGetter.apply(variants.get(0).getModelLocation()).resolveParents(modelGetter);
        } else if (size > 1) {
            ObjectOpenHashSet<ResourceLocation> seen = new ObjectOpenHashSet<>(size);
            for (Variant variant : variants) {
                ResourceLocation location = variant.getModelLocation();
                if (seen.add(location)) {
                    modelGetter.apply(location).resolveParents(modelGetter);
                }
            }
        }
    }
}
