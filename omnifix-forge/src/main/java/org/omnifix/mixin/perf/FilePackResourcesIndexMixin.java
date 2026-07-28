package org.omnifix.mixin.perf;

import com.mojang.logging.LogUtils;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.Nullable;
import org.omnifix.resources.ZipPackIndex;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.zip.ZipFile;

/**
 * FilePackResources scans the entire zip central directory on every {@code getNamespaces} /
 * {@code listResources}. Build a one-shot directory tree index and serve both from it.
 */
@Mixin(FilePackResources.class)
public abstract class FilePackResourcesIndexMixin {

    @Unique
    private static final Logger OMNIFIX$LOGGER = LogUtils.getLogger();

    @Final
    @Shadow
    private File file;

    @Shadow
    @Nullable
    private ZipFile getOrCreateZipFile() {
        return null;
    }

    @Unique
    @Nullable
    private volatile ZipPackIndex omnifix$packIndex;

    @Unique
    @Nullable
    private ZipPackIndex omnifix$getOrCreateIndex() {
        ZipPackIndex index = omnifix$packIndex;
        if (index == null) {
            synchronized (this) {
                index = omnifix$packIndex;
                if (index == null) {
                    if (getOrCreateZipFile() == null) {
                        return null;
                    }
                    try {
                        omnifix$packIndex = index = new ZipPackIndex(file.toPath());
                    } catch (IOException e) {
                        OMNIFIX$LOGGER.error("[OmniFix] Failed to build zip index for {}", file, e);
                    }
                }
            }
        }
        return index;
    }

    @Inject(method = "getNamespaces", at = @At("HEAD"), cancellable = true)
    private void omnifix$getNamespaces(PackType type, CallbackInfoReturnable<Set<String>> cir) {
        ZipPackIndex index = omnifix$getOrCreateIndex();
        if (index != null) {
            cir.setReturnValue(index.getNamespaces(type));
        }
    }

    @Inject(method = "listResources", at = @At("HEAD"), cancellable = true)
    private void omnifix$listResources(
            PackType packType,
            String namespace,
            String path,
            PackResources.ResourceOutput resourceOutput,
            CallbackInfo ci
    ) {
        ZipFile zf = getOrCreateZipFile();
        ZipPackIndex index = omnifix$getOrCreateIndex();
        if (index != null && zf != null) {
            index.listResources(packType, namespace, path, zf, resourceOutput);
            ci.cancel();
        }
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void omnifix$invalidateIndex(CallbackInfo ci) {
        omnifix$packIndex = null;
    }
}
