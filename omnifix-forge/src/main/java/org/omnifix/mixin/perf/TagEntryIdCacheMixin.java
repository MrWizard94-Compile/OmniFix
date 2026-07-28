package org.omnifix.mixin.perf;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Cache {@link TagEntry#elementOrTag()} results — hot path during datapack/tag resolve.
 */
@Mixin(TagEntry.class)
public abstract class TagEntryIdCacheMixin {

    @Shadow
    @Final
    private boolean tag;

    @Shadow
    @Final
    private ResourceLocation id;

    @Unique
    private ExtraCodecs.TagOrElementLocation omnifix$cachedLoc;

    /**
     * @author OmniFix (ModernFix-class)
     * @reason use cached location; overwrite to avoid inject allocs
     */
    @Overwrite
    private ExtraCodecs.TagOrElementLocation elementOrTag() {
        ExtraCodecs.TagOrElementLocation loc = omnifix$cachedLoc;
        if (loc == null) {
            loc = new ExtraCodecs.TagOrElementLocation(this.id, this.tag);
            omnifix$cachedLoc = loc;
        }
        return loc;
    }
}
