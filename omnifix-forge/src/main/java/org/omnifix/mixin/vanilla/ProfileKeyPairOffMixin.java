package org.omnifix.mixin.vanilla;

import com.mojang.authlib.minecraft.UserApiService;
import net.minecraft.client.User;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.nio.file.Path;

/**
 * Completes {@code feature.chat_signing_off}: never construct a live profile key-pair manager so
 * the client does not fetch/sign chat keys (complements {@link ChatSigningOffMixin} trust UI).
 *
 * <p>{@link ProfileKeyPairManager} is an interface with a static factory — only {@link Overwrite}
 * can replace it under Mixin 0.8.x.
 */
@Mixin(ProfileKeyPairManager.class)
public interface ProfileKeyPairOffMixin {

    /**
     * @author OmniFix
     * @reason Never allocate a signing key manager when chat signing is disabled.
     */
    @Overwrite
    static ProfileKeyPairManager create(UserApiService userApiService, User user, Path gameDirectory) {
        return ProfileKeyPairManager.EMPTY_KEY_MANAGER;
    }
}
