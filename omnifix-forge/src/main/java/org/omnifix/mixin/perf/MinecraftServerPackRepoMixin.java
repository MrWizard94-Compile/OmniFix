package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.resource.PathPackResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;

/** Prevent double-injection of Forge's pack finder into the same PackRepository. */
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerPackRepoMixin {

    @Unique
    private static final Set<PackRepository> OMNIFIX$INJECTED =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @WrapWithCondition(
            method = "configurePackRepository",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/resource/ResourcePackLoader;loadResourcePacks(Lnet/minecraft/server/packs/repository/PackRepository;Ljava/util/function/Function;)V"
            )
    )
    private static boolean omnifix$skipInjectIfAlreadyInjected(
            PackRepository resourcePacks,
            Function<Map<IModFile, ? extends PathPackResources>, ? extends RepositorySource> packFinder
    ) {
        return OMNIFIX$INJECTED.add(resourcePacks);
    }
}
