package org.omnifix.mixin.perf;

import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraftforge.registries.GameData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.AbstractList;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * DebugLevelSource.initValidStates rebuilds a full state list; reuse Forge's IdMapper view.
 */
@Mixin(DebugLevelSource.class)
public abstract class DebugLevelSourceStatesMixin {

    @Redirect(
            method = "initValidStates",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/stream/Stream;collect(Ljava/util/stream/Collector;)Ljava/lang/Object;",
                    ordinal = 0
            ),
            remap = false
    )
    private static Object omnifix$getStateList(Stream<?> instance, Collector<?, ?, ?> arCollector) {
        var idMapper = GameData.getBlockStateIDMap();
        return new AbstractList<>() {
            @Override
            public int size() {
                return idMapper.size();
            }

            @Override
            public Object get(int index) {
                var o = idMapper.byId(index);
                if (o == null) {
                    throw new IndexOutOfBoundsException();
                }
                return o;
            }
        };
    }
}
