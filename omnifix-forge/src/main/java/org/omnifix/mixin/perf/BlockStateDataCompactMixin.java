package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.fixes.BlockStateData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Intern constant NBT tags produced while parsing legacy BlockStateData mappings.
 */
@Mixin(value = BlockStateData.class, priority = 2000)
public abstract class BlockStateDataCompactMixin {

    @Unique
    private static ObjectOpenHashSet<Tag> OMNIFIX$TAG_INTERNER;

    @ModifyExpressionValue(
            method = "parse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/TagParser;parseTag(Ljava/lang/String;)Lnet/minecraft/nbt/CompoundTag;"
            )
    )
    private static CompoundTag omnifix$compactTag(CompoundTag tag) {
        if (OMNIFIX$TAG_INTERNER == null) {
            OMNIFIX$TAG_INTERNER = new ObjectOpenHashSet<>();
        }
        @SuppressWarnings("unchecked")
        Map.Entry<String, Tag>[] entries = new Map.Entry[tag.size()];
        int i = 0;
        for (String key : tag.getAllKeys()) {
            Tag t = tag.get(key);
            if (t instanceof CompoundTag ct) {
                t = omnifix$compactTag(ct);
            }
            t = OMNIFIX$TAG_INTERNER.addOrGet(t);
            entries[i++] = Map.entry(key.intern(), t);
        }
        return new CompoundTag(Map.ofEntries(entries));
    }

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void omnifix$clearInterner(CallbackInfo ci) {
        if (OMNIFIX$TAG_INTERNER != null) {
            OMNIFIX$TAG_INTERNER.clear();
            OMNIFIX$TAG_INTERNER.trim();
        }
    }
}
