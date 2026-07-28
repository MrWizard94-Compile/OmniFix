package org.omnifix.mixin.perf;

import net.minecraft.world.level.block.entity.ChestLidController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChestLidController.class)
public interface ChestLidControllerAccessor {
    @Accessor("shouldBeOpen")
    boolean omnifix$shouldBeOpen();

    @Accessor("openness")
    float omnifix$openness();

    @Accessor("oOpenness")
    void omnifix$setOOpenness(float value);
}
