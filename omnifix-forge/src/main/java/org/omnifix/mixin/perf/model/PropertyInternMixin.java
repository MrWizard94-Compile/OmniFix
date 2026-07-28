package org.omnifix.mixin.perf.model;

import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Intern property names so equals can use reference equality for name comparison.
 */
@Mixin(Property.class)
public abstract class PropertyInternMixin {

    @Shadow
    @Final
    private String name;

    @Shadow
    @Final
    private Class<?> clazz;

    @ModifyVariable(method = "<init>", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private static String omnifix$internName(String name) {
        return name.intern();
    }

    /**
     * @author OmniFix (ModernFix-class)
     * @reason reference equality after name interning
     */
    @Overwrite(remap = false)
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Property<?> property)) {
            return false;
        }
        //noinspection StringEquality
        return this.clazz == property.getValueClass() && this.name == property.getName();
    }
}
