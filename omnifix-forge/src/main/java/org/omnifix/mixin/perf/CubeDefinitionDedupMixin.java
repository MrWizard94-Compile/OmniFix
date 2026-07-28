package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Many entity model layers share identical cube definitions (same UV/origin/size/grow).
 * Deduplicate {@link ModelPart.Cube} instances produced by {@link CubeDefinition#bake}.
 */
@Mixin(CubeDefinition.class)
public abstract class CubeDefinitionDedupMixin {

    @Unique
    private static final ConcurrentHashMap<List<Object>, ModelPart.Cube> OMNIFIX$CUBE_CACHE =
            new ConcurrentHashMap<>();

    @WrapOperation(
            method = "bake",
            at = @At(
                    value = "NEW",
                    target = "(IIFFFFFFFFFZFFLjava/util/Set;)Lnet/minecraft/client/model/geom/ModelPart$Cube;"
            )
    )
    private ModelPart.Cube omnifix$deduplicateCube(
            int texCoordU,
            int texCoordV,
            float originX,
            float originY,
            float originZ,
            float dimensionX,
            float dimensionY,
            float dimensionZ,
            float growX,
            float growY,
            float growZ,
            boolean mirror,
            float texScaleU,
            float texScaleV,
            Set<?> visibleFaces,
            Operation<ModelPart.Cube> original
    ) {
        List<Object> cacheKey = List.of(
                texCoordU, texCoordV,
                originX, originY, originZ,
                dimensionX, dimensionY, dimensionZ,
                growX, growY, growZ,
                mirror, texScaleU, texScaleV,
                visibleFaces
        );
        ModelPart.Cube cube = OMNIFIX$CUBE_CACHE.get(cacheKey);
        if (cube == null) {
            cube = original.call(
                    texCoordU, texCoordV,
                    originX, originY, originZ,
                    dimensionX, dimensionY, dimensionZ,
                    growX, growY, growZ,
                    mirror, texScaleU, texScaleV,
                    visibleFaces
            );
            OMNIFIX$CUBE_CACHE.put(cacheKey, cube);
        }
        return cube;
    }
}
