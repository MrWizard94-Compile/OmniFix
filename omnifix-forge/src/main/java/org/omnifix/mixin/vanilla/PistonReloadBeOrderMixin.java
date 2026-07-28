package org.omnifix.mixin.vanilla;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

/**
 * MC-89146 — Pistons forget scheduled updates after chunk reload.
 *
 * <p>Root cause: {@link ChunkAccess#blockEntities} is a plain {@link java.util.HashMap}. Piston
 * move order depends on the order block entities are iterated/ticked after load. HashMap iteration
 * is unordered, so multi-piston chains can process in a different order after reload and drop
 * intermediate updates.
 *
 * <p>Fix: replace the field initializer with a linked open hash map so insertion/load order is
 * stable across saves. Mixin field-initializer shadow replaces the vanilla
 * {@code Maps.newHashMap()} assignment on 1.20.1 Mojmap.
 */
@Mixin(ChunkAccess.class)
public abstract class PistonReloadBeOrderMixin {

    @Shadow
    @Final
    @Mutable
    protected Map<BlockPos, BlockEntity> blockEntities = new Object2ObjectLinkedOpenHashMap<>();
}
