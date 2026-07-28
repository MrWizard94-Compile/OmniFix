package org.omnifix.mixin.perf;

import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collections;
import java.util.Map;

/**
 * Cache unmodifiable structure-reference map view; return emptyMap when empty to avoid iterator alloc.
 */
@Mixin(value = ChunkAccess.class, priority = 800)
public abstract class ChunkAccessStructureRefsMixin {

    @Shadow
    @Final
    private Map<?, ?> structuresRefences;

    @Unique
    private Map<?, ?> omnifix$structureRefsView;

    /**
     * @author OmniFix (ModernFix-class)
     * @reason cache map view / emptyMap for hot structure queries
     */
    @Overwrite
    public Map<?, ?> getAllReferences() {
        if (this.structuresRefences.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<?, ?> view = this.omnifix$structureRefsView;
        if (view == null) {
            this.omnifix$structureRefsView = view = Collections.unmodifiableMap(this.structuresRefences);
        }
        return view;
    }
}
