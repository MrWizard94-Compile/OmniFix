package org.omnifix.mixin.perf;

import com.google.common.collect.ImmutableSortedMap;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.omnifix.blockstate.FakeStateMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Map;

/**
 * With FerriteCore, use array-backed FakeStateMap for state construction instead of hashing/growing maps.
 */
@Mixin(StateDefinition.class)
public abstract class StateDefinitionFakeMapMixin<O, S extends StateHolder<O, S>> {

    @Shadow
    @Final
    private ImmutableSortedMap<String, Property<?>> propertiesByName;

    @ModifyVariable(method = "<init>", at = @At(value = "STORE", ordinal = 0), ordinal = 1, index = 8)
    private Map<Map<Property<?>, Comparable<?>>, S> omnifix$useArrayMap(
            Map<Map<Property<?>, Comparable<?>>, S> in
    ) {
        int numStates = 1;
        for (Property<?> prop : this.propertiesByName.values()) {
            numStates *= prop.getPossibleValues().size();
        }
        return new FakeStateMap<>(numStates);
    }
}
