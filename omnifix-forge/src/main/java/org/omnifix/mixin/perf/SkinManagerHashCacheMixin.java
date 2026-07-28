package org.omnifix.mixin.perf;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.resources.SkinManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * SkinManager.registerTexture calls {@link MinecraftProfileTexture#getHash()} repeatedly; hashing
 * re-digests the URL string. Cache the result for a short window (profiles re-register often during
 * tab list / skin refresh) to avoid redundant MessageDigest work on the client render thread.
 */
@Mixin(SkinManager.class)
public abstract class SkinManagerHashCacheMixin {

    @Unique
    private final Cache<MinecraftProfileTexture, String> omnifix$hashCache = CacheBuilder.newBuilder()
            .expireAfterAccess(60, TimeUnit.SECONDS)
            .concurrencyLevel(1)
            .build();

    @Redirect(
            method = "registerTexture(Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;Lnet/minecraft/client/resources/SkinManager$SkinTextureCallback;)Lnet/minecraft/resources/ResourceLocation;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;getHash()Ljava/lang/String;",
                    remap = false
            )
    )
    private String omnifix$useCachedHash(MinecraftProfileTexture texture) {
        String hash = omnifix$hashCache.getIfPresent(texture);
        if (hash != null) {
            return hash;
        }
        try {
            return omnifix$hashCache.get(texture, texture::getHash);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
