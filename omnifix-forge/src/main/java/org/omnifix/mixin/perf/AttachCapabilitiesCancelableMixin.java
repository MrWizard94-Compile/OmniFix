package org.omnifix.mixin.perf;

import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;

/**
 * AttachCapabilitiesEvent subclasses GenericEvent (EventBus library), so Forge's
 * EventSubclassTransformer never injects a constant isCancelable() override. Without it, every
 * event post hits EventListenerHelper.isCancelable(Class) reflection. Force constant false.
 */
@Mixin(AttachCapabilitiesEvent.class)
public abstract class AttachCapabilitiesCancelableMixin extends Event {

    @Override
    public boolean isCancelable() {
        return false;
    }
}
