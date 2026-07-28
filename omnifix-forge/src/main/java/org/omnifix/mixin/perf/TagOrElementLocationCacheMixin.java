package org.omnifix.mixin.perf;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Cache decorated id string for tag/element locations (companion to {@link TagEntryIdCacheMixin}).
 */
@Mixin(ExtraCodecs.TagOrElementLocation.class)
public abstract class TagOrElementLocationCacheMixin {

    @Shadow
    @Final
    private boolean tag;

    @Shadow
    @Final
    private ResourceLocation id;

    @Unique
    private String omnifix$cachedDecoratedId;

    /**
     * @author OmniFix (ModernFix-class)
     * @reason cache decorated id string
     */
    @Overwrite
    private String decoratedId() {
        String cached = omnifix$cachedDecoratedId;
        if (cached == null) {
            cached = this.tag ? "#" + this.id : this.id.toString();
            omnifix$cachedDecoratedId = cached;
        }
        return cached;
    }
}
