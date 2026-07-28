package org.omnifix.mixin.perf;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * When FerriteCore is absent, empty neighbour tables still use Array/Hash tables.
 * Replace with a shared empty ImmutableTable.
 */
@Mixin(StateHolder.class)
public abstract class StateHolderEmptyTableMixin {

    @Shadow
    private Table<Property<?>, Comparable<?>, ?> neighbours;

    @Inject(method = "populateNeighbours", at = @At("RETURN"), require = 0)
    private void omnifix$replaceEmptyTable(CallbackInfo ci) {
        if ((this.neighbours instanceof ArrayTable || this.neighbours instanceof HashBasedTable)
                && this.neighbours.isEmpty()) {
            this.neighbours = ImmutableTable.of();
        }
    }
}
