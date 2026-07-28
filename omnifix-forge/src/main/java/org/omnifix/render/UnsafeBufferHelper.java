package org.omnifix.render;

import org.lwjgl.system.MemoryUtil;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

/**
 * Frees native memory behind leaked {@link ByteBuffer}s from {@link com.mojang.blaze3d.vertex.BufferBuilder}.
 * Mirrors ModernFix's helper (LGPL port pattern / independent reimplementation).
 */
public final class UnsafeBufferHelper {

    private static final MemoryUtil.MemoryAllocator ALLOCATOR = MemoryUtil.getAllocator(false);

    private static Unsafe UNSAFE;
    private static long ADDRESS = -1;

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);

            Field addressField = MemoryUtil.class.getDeclaredField("ADDRESS");
            addressField.setAccessible(true);
            ADDRESS = addressField.getLong(null);
        } catch (Throwable ignored) {
            UNSAFE = null;
            ADDRESS = -1;
        }
    }

    private UnsafeBufferHelper() {}

    public static void init() {
        // Force class load early (avoid Forge event-transformer noise).
    }

    public static void free(ByteBuffer buf) {
        if (UNSAFE != null && ADDRESS >= 0) {
            long address = UNSAFE.getAndSetLong(buf, ADDRESS, 0);
            if (address != 0) {
                ALLOCATOR.free(address);
            }
        } else {
            ALLOCATOR.free(MemoryUtil.memAddress0(buf));
        }
    }
}
