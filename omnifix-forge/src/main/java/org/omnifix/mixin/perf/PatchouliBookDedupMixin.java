package org.omnifix.mixin.perf;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Patchouli: after book reload, replace AIR ItemStacks on template components with
 * {@link ItemStack#EMPTY} so each empty slot does not retain a unique NBT-bearing stack instance.
 */
@Pseudo
@Mixin(targets = "vazkii.patchouli.client.book.ClientBookRegistry", remap = false)
public abstract class PatchouliBookDedupMixin {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Inject(method = "reload", at = @At("RETURN"), remap = false)
    private void omnifix$dedupeAirStacks(CallbackInfo ci) {
        try {
            Class<?> pageTemplateClz = Class.forName("vazkii.patchouli.client.book.page.PageTemplate");
            Class<?> bookClz = Class.forName("vazkii.patchouli.common.book.Book");
            Class<?> bookTemplateClz = Class.forName("vazkii.patchouli.client.book.template.BookTemplate");
            Class<?> componentItemClz =
                    Class.forName("vazkii.patchouli.client.book.template.component.ComponentItemStack");
            Class<?> bookRegistryClz = Class.forName("vazkii.patchouli.common.book.BookRegistry");

            Field templateField = ObfuscationReflectionHelper.findField(pageTemplateClz, "template");
            Field contentsField = ObfuscationReflectionHelper.findField(bookClz, "contents");
            Field componentsField = ObfuscationReflectionHelper.findField(bookTemplateClz, "components");
            Field itemsField = ObfuscationReflectionHelper.findField(componentItemClz, "items");
            Field booksField = bookRegistryClz.getField("INSTANCE");
            Object registry = booksField.get(null);
            Field booksMapField = registry.getClass().getField("books");
            @SuppressWarnings("unchecked")
            Map<?, ?> books = (Map<?, ?>) booksMapField.get(registry);

            int numItemsCleared = 0;
            for (Object book : books.values()) {
                Object contents = contentsField.get(book);
                if (contents == null) {
                    continue;
                }
                Field entriesField = contents.getClass().getField("entries");
                @SuppressWarnings("unchecked")
                Map<?, ?> entries = (Map<?, ?>) entriesField.get(contents);
                if (entries == null) {
                    continue;
                }
                for (Object entry : entries.values()) {
                    @SuppressWarnings("unchecked")
                    List<?> pages = (List<?>) entry.getClass().getMethod("getPages").invoke(entry);
                    for (Object page : pages) {
                        if (!pageTemplateClz.isInstance(page)) {
                            continue;
                        }
                        Object template = templateField.get(page);
                        if (template == null) {
                            continue;
                        }
                        @SuppressWarnings("unchecked")
                        List<?> components = (List<?>) componentsField.get(template);
                        if (components == null) {
                            continue;
                        }
                        for (Object component : components) {
                            if (!componentItemClz.isInstance(component)) {
                                continue;
                            }
                            ItemStack[] items = (ItemStack[]) itemsField.get(component);
                            if (items == null) {
                                continue;
                            }
                            for (int i = 0; i < items.length; i++) {
                                if (items[i] != null && items[i].getItem() == Items.AIR) {
                                    numItemsCleared++;
                                    items[i] = ItemStack.EMPTY;
                                }
                            }
                        }
                    }
                }
            }
            if (numItemsCleared > 0) {
                LOGGER.info("[OmniFix] Cleared {} unneeded Patchouli book NBT tags", numItemsCleared);
            }
        } catch (Throwable t) {
            LOGGER.debug("[OmniFix] Patchouli book dedup skipped", t);
        }
    }
}
