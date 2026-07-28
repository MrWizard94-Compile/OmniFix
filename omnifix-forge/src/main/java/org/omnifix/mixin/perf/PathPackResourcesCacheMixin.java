package org.omnifix.mixin.perf;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.resource.PathPackResources;
import org.jetbrains.annotations.NotNull;
import org.omnifix.resources.ICachingResourcePack;
import org.omnifix.resources.PackResourcesCacheEngine;
import org.omnifix.util.PackTypeHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

/**
 * Cache PathPackResources (mod folder/jar path packs) directory trees for namespaces/list/exists.
 */
@Mixin(value = PathPackResources.class, priority = 1100)
public abstract class PathPackResourcesCacheMixin implements ICachingResourcePack {

    @Shadow(remap = false)
    protected abstract Path resolve(String... paths);

    @Shadow(remap = false)
    @NotNull
    protected abstract Set<String> getNamespacesFromDisk(PackType type);

    @Unique
    private PackResourcesCacheEngine omnifix$cacheEngine;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void omnifix$cacheResources(String packId, boolean isBuiltin, Path source, CallbackInfo ci) {
        invalidateCache();
    }

    @Unique
    private PackResourcesCacheEngine omnifix$generateResourceCache() {
        synchronized (this) {
            PackResourcesCacheEngine engine = this.omnifix$cacheEngine;
            if (engine != null) {
                return engine;
            }
            this.omnifix$cacheEngine = engine = new PackResourcesCacheEngine(type -> this.resolve(type.getDirectory()));
            return engine;
        }
    }

    @Override
    public void invalidateCache() {
        this.omnifix$cacheEngine = null;
    }

    @Redirect(
            method = "getNamespaces",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/resource/PathPackResources;getNamespacesFromDisk(Lnet/minecraft/server/packs/PackType;)Ljava/util/Set;"
            )
    )
    private Set<String> omnifix$useCacheForNamespaces(PathPackResources instance, PackType type) {
        PackResourcesCacheEngine engine = omnifix$cacheEngine;
        if (engine != null) {
            Set<String> namespaces = engine.getNamespaces(type);
            if (namespaces != null) {
                return namespaces;
            }
        }
        return this.getNamespacesFromDisk(type);
    }

    @Redirect(
            method = "getRootResource",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/nio/file/Files;exists(Ljava/nio/file/Path;[Ljava/nio/file/LinkOption;)Z"
            )
    )
    private boolean omnifix$useCacheForExistence(Path path, LinkOption[] options, String[] originalPaths) {
        if (originalPaths.length < 3
                || (!Objects.equals(originalPaths[0], "assets") && !Objects.equals(originalPaths[0], "data"))) {
            return Files.exists(path, options);
        }
        return this.omnifix$generateResourceCache().hasResource(originalPaths);
    }

    @Inject(method = "listResources", at = @At("HEAD"), cancellable = true)
    private void omnifix$fastGetResources(
            PackType type,
            String namespace,
            String path,
            PackResources.ResourceOutput resourceOutput,
            CallbackInfo ci
    ) {
        if (!PackTypeHelper.isVanillaPackType(type)) {
            return;
        }
        ci.cancel();
        this.omnifix$generateResourceCache().collectResources(
                type,
                namespace,
                PackResourcesCacheEngine.decomposeCached(path),
                Integer.MAX_VALUE,
                resourceOutput
        );
    }
}
