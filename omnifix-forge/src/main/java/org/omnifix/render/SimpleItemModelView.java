package org.omnifix.render;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Presents only camera-facing quads of a simple item/block model for 2D GUI rendering.
 */
public final class SimpleItemModelView implements BakedModel {

    private BakedModel wrappedItem;
    private FastItemRenderType type;
    private final List<BakedQuad> filtered = new ObjectArrayList<>();

    public void setItem(BakedModel model) {
        this.wrappedItem = model;
    }

    public void setType(FastItemRenderType type) {
        this.type = type;
    }

    private boolean isCorrectDirectionForType(Direction direction) {
        if (type == FastItemRenderType.SIMPLE_ITEM) {
            return direction == Direction.SOUTH;
        }
        return direction == Direction.UP || direction == Direction.EAST || direction == Direction.NORTH;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        boolean wholeValid = isCorrectDirectionForType(side);
        List<BakedQuad> realList = wrappedItem.getQuads(state, side, rand);
        if (wholeValid) {
            return realList;
        }
        filtered.clear();
        for (int i = 0; i < realList.size(); i++) {
            BakedQuad quad = realList.get(i);
            if (isCorrectDirectionForType(quad.getDirection())) {
                filtered.add(quad);
            }
        }
        return filtered;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return wrappedItem.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return wrappedItem.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return wrappedItem.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return wrappedItem.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return wrappedItem.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return wrappedItem.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return wrappedItem.getOverrides();
    }
}
