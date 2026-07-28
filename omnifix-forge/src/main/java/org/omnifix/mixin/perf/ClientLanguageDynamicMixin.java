package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.server.packs.resources.Resource;
import org.omnifix.dynamiclanguages.DynamicLanguageMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mixin(value = ClientLanguage.class, priority = 2000)
public abstract class ClientLanguageDynamicMixin {

    @WrapOperation(
            method = "loadFrom",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/language/ClientLanguage;appendFrom(Ljava/lang/String;Ljava/util/List;Ljava/util/Map;)V"
            )
    )
    private static void omnifix$collectResources(
            String languageName,
            List<Resource> resources,
            Map<String, String> destinationMap,
            Operation<Void> original,
            @Share("usedResources") LocalRef<List<Resource>> usedResources
    ) {
        List<Resource> collected = usedResources.get();
        if (collected == null) {
            collected = new ArrayList<>();
            usedResources.set(collected);
        }
        collected.addAll(resources);
        original.call(languageName, resources, destinationMap);
    }

    @ModifyArg(
            method = "loadFrom",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/language/ClientLanguage;<init>(Ljava/util/Map;Z)V"
            ),
            index = 0
    )
    private static Map<String, String> omnifix$modifyLanguageMap(
            Map<String, String> storage,
            @Share("usedResources") LocalRef<List<Resource>> usedResources
    ) {
        List<Resource> collected = Objects.requireNonNullElse(usedResources.get(), List.of());
        return DynamicLanguageMap.forVanillaData(storage, collected);
    }
}
