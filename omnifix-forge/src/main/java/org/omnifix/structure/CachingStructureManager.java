package org.omnifix.structure;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Disk-caches DFU-upgraded structure NBTs so outdated structure files do not re-run DFU every load.
 */
public final class CachingStructureManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_HASH_LENGTH = 9;

    private static final ThreadLocal<MessageDigest> DIGEST = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    });

    private static final File STRUCTURE_CACHE_FOLDER = childFile(
            FMLPaths.GAMEDIR.get().resolve("omnifix").resolve("structureCacheV1").toFile());

    static {
        STRUCTURE_CACHE_FOLDER.mkdirs();
    }

    private CachingStructureManager() {}

    public static StructureTemplate readStructure(
            ResourceLocation location,
            DataFixer datafixer,
            InputStream stream,
            HolderGetter<Block> blockGetter
    ) throws IOException {
        CompoundTag tag = readStructureTag(location, datafixer, stream);
        StructureTemplate template = new StructureTemplate();
        template.load(blockGetter, tag);
        return template;
    }

    public static CompoundTag readStructureTag(
            ResourceLocation location,
            DataFixer datafixer,
            InputStream stream
    ) throws IOException {
        byte[] structureBytes = toBytes(stream);
        CompoundTag currentTag = NbtIo.readCompressed(new ByteArrayInputStream(structureBytes));
        if (!currentTag.contains("DataVersion", 99)) {
            currentTag.putInt("DataVersion", 500);
        }
        int currentDataVersion = currentTag.getInt("DataVersion");
        int required = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
        if (currentDataVersion < required) {
            MessageDigest hasher = DIGEST.get();
            hasher.reset();
            String hash = encodeHex(hasher.digest(structureBytes));
            CompoundTag cached = getCachedUpgraded(location, truncateHash(hash));
            if (cached != null && cached.getInt("DataVersion") == required) {
                LOGGER.debug("[OmniFix] Using cached upgraded structure {}", location);
                currentTag = cached;
            } else {
                LOGGER.debug("[OmniFix] Structure {} DFU upgrade (hash {})", location, hash);
                currentTag = DataFixTypes.STRUCTURE.update(datafixer, currentTag, currentDataVersion, required);
                currentTag.putInt("DataVersion", required);
                saveCachedUpgraded(location, hash, currentTag);
            }
        }
        return currentTag;
    }

    private static String truncateHash(String hash) {
        return hash.substring(0, MAX_HASH_LENGTH + 1);
    }

    private static String encodeHex(byte[] byteArray) {
        StringBuilder sb = new StringBuilder(byteArray.length * 2);
        for (byte b : byteArray) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static File getCachePath(ResourceLocation location, String hash) {
        String fileName = location.getNamespace() + "_" + location.getPath().replace('/', '_') + "_" + hash + ".nbt";
        return new File(STRUCTURE_CACHE_FOLDER, fileName);
    }

    private static synchronized CompoundTag getCachedUpgraded(ResourceLocation location, String hash) {
        File theFile = getCachePath(location, hash);
        try {
            return NbtIo.readCompressed(theFile);
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            LOGGER.warn("[OmniFix] Failed reading structure cache {}", theFile, e);
            return null;
        }
    }

    private static synchronized void saveCachedUpgraded(ResourceLocation location, String hash, CompoundTag tag) {
        File theFile = getCachePath(location, truncateHash(hash));
        try {
            NbtIo.writeCompressed(tag, theFile);
        } catch (IOException e) {
            LOGGER.warn("[OmniFix] Failed writing structure cache {}", theFile, e);
        }
    }

    private static byte[] toBytes(InputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] tmp = new byte[16384];
        int n;
        while ((n = stream.read(tmp, 0, tmp.length)) != -1) {
            buffer.write(tmp, 0, n);
        }
        return buffer.toByteArray();
    }

    private static File childFile(File file) {
        file.getParentFile().mkdirs();
        return file;
    }
}
