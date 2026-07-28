package org.omnifix.mixin.perf;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.datafixers.DataFixer;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;

/**
 * StructureTemplateManager holds every loaded template forever. SoftValues allow GC under pressure
 * (templates reload cheaply with our DFU structure cache).
 */
@Mixin(StructureTemplateManager.class)
public abstract class StructureRepositorySoftCacheMixin {

    @Shadow
    @Final
    @Mutable
    private Map<ResourceLocation, Optional<StructureTemplate>> structureRepository;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void omnifix$makeStructuresSoft(
            ResourceManager resourceManager,
            LevelStorageSource.LevelStorageAccess access,
            DataFixer dataFixer,
            HolderGetter<Block> blockGetter,
            CallbackInfo ci
    ) {
        Cache<ResourceLocation, Optional<StructureTemplate>> structureCache = CacheBuilder.newBuilder()
                .softValues()
                .build();
        this.structureRepository = structureCache.asMap();
    }
}
