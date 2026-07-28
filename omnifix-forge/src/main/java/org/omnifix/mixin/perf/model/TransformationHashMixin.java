package org.omnifix.mixin.perf.model;

import com.mojang.math.Transformation;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Objects;

/**
 * Cache {@link Transformation#hashCode()} — matrices are immutable after construction.
 */
@Mixin(Transformation.class)
public abstract class TransformationHashMixin {

    @Shadow
    @Final
    private Matrix4f matrix;

    @Unique
    private Integer omnifix$cachedHash;

    /**
     * @author OmniFix (ModernFix-class)
     * @reason cache matrix hash
     */
    @Overwrite(remap = false)
    public int hashCode() {
        Integer cached = omnifix$cachedHash;
        if (cached != null) {
            return cached;
        }
        int hash = Objects.hashCode(this.matrix);
        omnifix$cachedHash = hash;
        return hash;
    }
}
