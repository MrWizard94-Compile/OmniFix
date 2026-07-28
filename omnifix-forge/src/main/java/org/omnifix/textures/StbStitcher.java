package org.omnifix.textures;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.StitcherException;
import net.minecraft.util.Mth;
import org.lwjgl.stb.STBRPContext;
import org.lwjgl.stb.STBRPNode;
import org.lwjgl.stb.STBRPRect;
import org.lwjgl.stb.STBRectPack;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;

/**
 * STB rectangle packer for large texture atlases. Ported from lwjgl3ify / ModernFix approach with
 * dual short/int STBRPRect handles for LWJGL ABI variance.
 */
public final class StbStitcher {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final MethodHandle MH_RECT_SHORT_SET;
    private static final MethodHandle MH_RECT_INT_SET;
    private static final MethodHandle MH_RECT_SHORT_X;
    private static final MethodHandle MH_RECT_INT_X;
    private static final MethodHandle MH_RECT_SHORT_Y;
    private static final MethodHandle MH_RECT_INT_Y;

    static {
        MethodHandle shortSet = null;
        MethodHandle intSet = null;
        List<ReflectiveOperationException> exceptions = new ArrayList<>();
        try {
            intSet = publicLookup().findVirtual(STBRPRect.class, "set", methodType(
                    STBRPRect.class, int.class, int.class, int.class, int.class, int.class, boolean.class));
        } catch (ReflectiveOperationException e) {
            exceptions.add(e);
        }
        try {
            shortSet = publicLookup().findVirtual(STBRPRect.class, "set", methodType(
                    STBRPRect.class, int.class, short.class, short.class, short.class, short.class, boolean.class));
        } catch (ReflectiveOperationException e) {
            exceptions.add(e);
        }
        if (shortSet == null && intSet == null) {
            IllegalStateException e = new IllegalStateException("An STBRPRect set method could not be located");
            exceptions.forEach(e::addSuppressed);
            throw e;
        }
        MH_RECT_SHORT_SET = shortSet;
        MH_RECT_INT_SET = intSet;

        MethodHandle shortX = null;
        MethodHandle intX = null;
        exceptions.clear();
        try {
            intX = publicLookup().findVirtual(STBRPRect.class, "x", methodType(int.class));
        } catch (ReflectiveOperationException e) {
            exceptions.add(e);
        }
        try {
            shortX = publicLookup().findVirtual(STBRPRect.class, "x", methodType(short.class));
        } catch (ReflectiveOperationException e) {
            exceptions.add(e);
        }
        if (shortX == null && intX == null) {
            IllegalStateException e = new IllegalStateException("An STBRPRect x() method could not be located");
            exceptions.forEach(e::addSuppressed);
            throw e;
        }
        MH_RECT_SHORT_X = shortX;
        MH_RECT_INT_X = intX;
        try {
            if (MH_RECT_SHORT_X != null) {
                MH_RECT_SHORT_Y = publicLookup().findVirtual(STBRPRect.class, "y", methodType(short.class));
                MH_RECT_INT_Y = null;
            } else {
                MH_RECT_INT_Y = publicLookup().findVirtual(STBRPRect.class, "y", methodType(int.class));
                MH_RECT_SHORT_Y = null;
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("An STBRPRect y() method could not be located", e);
        }
    }

    private StbStitcher() {}

    public static STBRPRect setWrapper(
            STBRPRect rect, int id, int width, int height, int x, int y, boolean wasPacked) {
        try {
            if (MH_RECT_SHORT_SET != null) {
                return (STBRPRect) MH_RECT_SHORT_SET.invokeExact(
                        rect, id, (short) width, (short) height, (short) x, (short) y, wasPacked);
            }
            return (STBRPRect) MH_RECT_INT_SET.invokeExact(rect, id, width, height, x, y, wasPacked);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    public static int getX(STBRPRect rect) {
        try {
            if (MH_RECT_SHORT_X != null) {
                return (short) MH_RECT_SHORT_X.invokeExact(rect);
            }
            return (int) MH_RECT_INT_X.invokeExact(rect);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    public static int getY(STBRPRect rect) {
        try {
            if (MH_RECT_SHORT_X != null) {
                return (short) MH_RECT_SHORT_Y.invokeExact(rect);
            }
            return (int) MH_RECT_INT_Y.invokeExact(rect);
        } catch (Throwable e) {
            throw new AssertionError(e);
        }
    }

    public static <T extends Stitcher.Entry> Pair<Pair<Integer, Integer>, List<LoadableSpriteInfo<T>>> packRects(
            Stitcher.Holder<T>[] holders) {
        int holderSize = holders.length;
        List<LoadableSpriteInfo<T>> infoList = new ArrayList<>();

        try (STBRPRect.Buffer rectBuf = STBRPRect.malloc(holderSize);
                STBRPContext ctx = STBRPContext.malloc()) {
            int totalArea = 0;
            int longestWidth = 0;
            int longestHeight = 0;
            for (int j = 0; j < holderSize; ++j) {
                Stitcher.Holder<T> holder = holders[j];
                int width = holder.width();
                int height = holder.height();
                STBRPRect rect = rectBuf.get(j);
                setWrapper(rect, j, width, height, 0, 0, false);
                totalArea += width * height;
                longestWidth = Math.max(longestWidth, width);
                longestHeight = Math.max(longestHeight, height);
            }

            longestWidth = Mth.smallestEncompassingPowerOfTwo(longestWidth);
            longestHeight = Mth.smallestEncompassingPowerOfTwo(longestHeight);

            while (longestWidth * longestHeight < totalArea) {
                if (longestWidth <= longestHeight) {
                    longestWidth *= 2;
                } else {
                    longestHeight *= 2;
                }
            }

            int numTries = 0;
            while (true) {
                numTries++;
                try (STBRPNode.Buffer nodes = STBRPNode.malloc(longestWidth + 10)) {
                    STBRectPack.stbrp_init_target(ctx, longestWidth, longestHeight, nodes);
                    STBRectPack.stbrp_pack_rects(ctx, rectBuf);

                    for (STBRPRect rect : rectBuf) {
                        Stitcher.Holder<T> holder = holders[rect.id()];
                        if (!rect.was_packed()) {
                            throw new StitcherException(
                                    holder.entry(),
                                    Stream.of(holders)
                                            .map(Stitcher.Holder::entry)
                                            .collect(ImmutableList.toImmutableList()));
                        }
                    }

                    for (STBRPRect rect : rectBuf) {
                        Stitcher.Holder<T> holder = holders[rect.id()];
                        infoList.add(new LoadableSpriteInfo<>(
                                holder.entry(), longestWidth, longestHeight, getX(rect), getY(rect)));
                    }
                    return Pair.of(Pair.of(longestWidth, longestHeight), infoList);
                } catch (StitcherException e) {
                    if (numTries >= 4) {
                        LOGGER.error(
                                "[OmniFix] STB stitcher out of space at {}x{}:",
                                longestWidth,
                                longestHeight);
                        for (Stitcher.Holder<T> h : holders) {
                            LOGGER.error(" - {}, {}x{}", h.entry().name(), h.width(), h.height());
                        }
                        throw e;
                    }
                    if (longestWidth <= longestHeight) {
                        longestWidth *= 2;
                    } else {
                        longestHeight *= 2;
                    }
                }
            }
        }
    }

    public static final class LoadableSpriteInfo<T extends Stitcher.Entry> {
        public final T info;
        public final int width;
        public final int height;
        public final int x;
        public final int y;

        LoadableSpriteInfo(T info, int width, int height, int x, int y) {
            this.info = info;
            this.width = width;
            this.height = height;
            this.x = x;
            this.y = y;
        }
    }
}
