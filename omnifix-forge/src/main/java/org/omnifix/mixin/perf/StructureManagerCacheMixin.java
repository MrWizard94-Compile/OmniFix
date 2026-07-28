package org.omnifix.mixin.perf;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.omnifix.structure.CachingStructureManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Route structure NBT loads through a DFU-upgrade disk cache.
 */
@Mixin(StructureTemplateManager.class)
public abstract class StructureManagerCacheMixin {

    @Unique
    private static final Logger OMNIFIX$LOGGER = LogUtils.getLogger();

    @Shadow
    @Final
    private DataFixer fixerUpper;

    @Shadow
    private ResourceManager resourceManager;

    @Shadow
    @Final
    private HolderGetter<Block> blockLookup;

    /**
     * @author OmniFix
     * @reason use CachingStructureManager to avoid needless DFU updates
     */
    @Overwrite
    private Optional<StructureTemplate> loadFromResource(ResourceLocation id) {
        ResourceLocation path = new ResourceLocation(id.getNamespace(), "structures/" + id.getPath() + ".nbt");
        try (InputStream stream = this.resourceManager.open(path)) {
            return Optional.of(CachingStructureManager.readStructure(id, this.fixerUpper, stream, this.blockLookup));
        } catch (FileNotFoundException e) {
            return Optional.empty();
        } catch (IOException e) {
            OMNIFIX$LOGGER.error("[OmniFix] Can't read structure {}", id, e);
            return Optional.empty();
        }
    }
}
