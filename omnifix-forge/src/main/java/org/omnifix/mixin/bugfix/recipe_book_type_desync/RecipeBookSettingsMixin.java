package org.omnifix.mixin.bugfix.recipe_book_type_desync;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.stats.RecipeBookSettings;
import net.minecraft.world.inventory.RecipeBookType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * When mods add {@link RecipeBookType} entries, {@link RecipeBookSettings#read} still iterates all
 * ordinals. A vanilla-sized packet (or any short packet) then desyncs the buffer.
 *
 * <p>If the type is past the last public vanilla ordinal and fewer than 2 bytes remain, skip the read
 * and return a default {@code false} (open/filtering flags).
 */
@Mixin(RecipeBookSettings.class)
public abstract class RecipeBookSettingsMixin {

    @Unique
    private static final int OMNIFIX$MAX_VANILLA_ORDINAL;

    static {
        int ord = 0;
        for (Field f : RecipeBookType.class.getDeclaredFields()) {
            if (RecipeBookType.class.isAssignableFrom(f.getType())
                    && Modifier.isStatic(f.getModifiers())
                    && Modifier.isPublic(f.getModifiers())) {
                try {
                    f.setAccessible(true);
                    RecipeBookType type = (RecipeBookType) f.get(null);
                    ord = Math.max(type.ordinal(), ord);
                } catch (Exception e) {
                    ord = Integer.MAX_VALUE - 1;
                    break;
                }
            }
        }
        OMNIFIX$MAX_VANILLA_ORDINAL = ord;
    }

    @Redirect(
            method = "read(Lnet/minecraft/network/FriendlyByteBuf;)Lnet/minecraft/stats/RecipeBookSettings;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;readBoolean()Z")
    )
    private static boolean omnifix$guardModdedTypes(FriendlyByteBuf buf, @Local(ordinal = 0) RecipeBookType type) {
        if (type.ordinal() > OMNIFIX$MAX_VANILLA_ORDINAL && buf.readableBytes() < 1) {
            return false;
        }
        return buf.readBoolean();
    }
}
