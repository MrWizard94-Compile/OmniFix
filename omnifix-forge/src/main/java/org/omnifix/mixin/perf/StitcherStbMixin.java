package org.omnifix.mixin.perf;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.StitcherException;
import org.omnifix.textures.StbStitcher;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Replaces vanilla {@link Stitcher} packing with STB for atlases ≥100 sprites. Small atlases keep
 * vanilla packing so mods that rely on exact alignments (e.g. some JEI paths) stay stable.
 */
@Mixin(Stitcher.class)
public abstract class StitcherStbMixin<T extends Stitcher.Entry> {

    @Unique
    private static final Logger OMNIFIX$LOGGER = LogUtils.getLogger();

    @Shadow
    @Final
    private List<Stitcher.Holder<T>> texturesToBeStitched;

    @Shadow
    private int storageX;

    @Shadow
    private int storageY;

    @Shadow
    @Final
    private int maxWidth;

    @Shadow
    @Final
    private int maxHeight;

    @Shadow
    @Final
    private static Comparator<Stitcher.Holder<?>> HOLDER_COMPARATOR;

    @Unique
    private List<StbStitcher.LoadableSpriteInfo<T>> omnifix$loadableSpriteInfos;

    @Inject(method = "stitch", at = @At("HEAD"), cancellable = true)
    private void omnifix$stitchFast(CallbackInfo ci) {
        this.omnifix$loadableSpriteInfos = null;
        if (this.texturesToBeStitched.size() < 100) {
            return;
        }
        ci.cancel();
        ObjectArrayList<Stitcher.Holder<T>> holderList = new ObjectArrayList<>(this.texturesToBeStitched);
        holderList.sort(HOLDER_COMPARATOR);
        @SuppressWarnings("unchecked")
        Stitcher.Holder<T>[] aholder = holderList.toArray(new Stitcher.Holder[0]);

        Pair<Pair<Integer, Integer>, List<StbStitcher.LoadableSpriteInfo<T>>> packingInfo =
                StbStitcher.packRects(aholder);
        this.storageX = packingInfo.getFirst().getFirst();
        this.storageY = packingInfo.getFirst().getSecond();

        if (this.storageX > this.maxWidth || this.storageY > this.maxHeight) {
            OMNIFIX$LOGGER.error(
                    "[OmniFix] Requested atlas size {}x{} exceeds maximum of {}x{}",
                    this.storageX,
                    this.storageY,
                    this.maxWidth,
                    this.maxHeight);
            throw new StitcherException(
                    aholder[0].entry(),
                    Stream.of(aholder).map(Stitcher.Holder::entry).collect(ImmutableList.toImmutableList()));
        }
        this.omnifix$loadableSpriteInfos = packingInfo.getSecond();
    }

    @Inject(method = "gatherSprites", at = @At("HEAD"), cancellable = true)
    private void omnifix$gatherSpritesFast(Stitcher.SpriteLoader<T> spriteLoader, CallbackInfo ci) {
        if (this.omnifix$loadableSpriteInfos == null) {
            return;
        }
        ci.cancel();
        for (StbStitcher.LoadableSpriteInfo<T> info : this.omnifix$loadableSpriteInfos) {
            spriteLoader.load(info.info, info.x, info.y);
        }
    }
}
