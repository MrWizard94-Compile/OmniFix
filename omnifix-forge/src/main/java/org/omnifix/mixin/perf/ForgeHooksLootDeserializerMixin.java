package org.omnifix.mixin.perf;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.common.ForgeHooks;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

/**
 * Uses the builtin-table marker from {@link LootDataManagerFasterMixin} so Forge does not re-probe
 * pack resources for every table when deciding vanilla-vs-custom for {@code loadLootTable} events.
 *
 * <p>Matches Forge: {@code custom = !isBuiltin} for tables that were present in the scan.
 *
 * @author OmniFix
 */
@Mixin(value = ForgeHooks.class, remap = false)
public abstract class ForgeHooksLootDeserializerMixin {

    @Shadow
    @Final
    private static Logger LOGGER;

    private static boolean omnifix$isBuiltinTable(JsonElement data) {
        if (!(data instanceof JsonObject obj)) {
            return false;
        }
        var marker = obj.getAsJsonPrimitive("omnifix$isBuiltinTable");
        return marker != null && marker.getAsBoolean();
    }

    /**
     * @author OmniFix
     * @reason Avoid per-table getResource() by using pre-scan builtin marker.
     */
    @Overwrite
    public static TriFunction<ResourceLocation, JsonElement, ResourceManager, Optional<LootTable>> getLootTableDeserializer(
            Gson gson, String directory) {
        return (location, data, resourceManager) -> {
            try {
                // Forge: custom when missing or non-builtin. Scanned entries exist; treat unmarked as custom.
                boolean custom = !omnifix$isBuiltinTable(data);
                return Optional.ofNullable(ForgeHooks.loadLootTable(gson, location, data, custom));
            } catch (Exception exception) {
                LOGGER.error("Couldn't parse element {}:{}", directory, location, exception);
                return Optional.empty();
            }
        };
    }
}
