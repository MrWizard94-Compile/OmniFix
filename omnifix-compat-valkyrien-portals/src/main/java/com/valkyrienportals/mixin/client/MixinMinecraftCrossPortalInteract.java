package com.valkyrienportals.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.mod.mixinducks.client.MinecraftDuck;
import qouteall.imm_ptl.core.block_manipulation.BlockManipulationClient;

/**
 * VS #1525 — Immersive Portals cross-portal interaction broken with Valkyrien Skies installed.
 *
 * <p><b>Root cause.</b> VS's client interaction pipeline has two cooperating pieces:
 * <ol>
 *   <li>{@code MixinGameRenderer#modifyCrosshairTargetBlocks} stores a ship-space-untransformed
 *       local raycast as {@code MinecraftDuck.originalCrosshairTarget} during {@code pick}.</li>
 *   <li>{@code MixinMinecraft#useOriginalCrosshairForBlockPlacement} wraps
 *       {@code MultiPlayerGameMode.useItemOn} inside {@code startUseItem} and
 *       <em>always</em> substitutes that stored original for the live {@code hitResult}.</li>
 * </ol>
 * IP's pipeline runs after {@code pick}: {@code BlockManipulationClient.updatePointedBlock} detects
 * a portal, forces local {@code hitResult} to a miss, and stores the far-side hit in
 * {@code remoteHitResult}. On use, IP wraps {@code startUseItem} with
 * {@code withSwitchedContext(..., transformHitResult=true)}, which temporarily sets
 * {@code hitResult} to the (optionally placement-adjusted) remote hit and switches the client
 * level. VS's wrap then discards that remote hit and feeds {@code useItemOn} the pre-portal local
 * original instead.
 *
 * <p>Observed symptoms match exactly:
 * <ul>
 *   <li>Complete failure to place/interact when the local raycast misses.</li>
 *   <li>Offset placement by the portal origin delta when the destination lies near the player in
 *       the same dimension (local original hits a nearby block at the wrong coordinates).</li>
 * </ul>
 * This reproduces with no ships present — VS's placement wrap is unconditional.
 *
 * <p><b>Fix.</b> At the head of {@code startUseItem}, once IP has switched context (so
 * {@code hitResult} is already the remote block hit), write that hit into VS's
 * {@code originalCrosshairTarget}. VS's wrap then forwards the correct cross-portal target.
 * Outside portal pointing the field is left alone so ship placement keeps working.
 */
@Mixin(value = Minecraft.class, priority = 1200)
public abstract class MixinMinecraftCrossPortalInteract {

    @Shadow
    public HitResult hitResult;

    @Inject(method = "startUseItem", at = @At("HEAD"), remap = true)
    private void omnifix$feedIpHitToVsOriginalCrosshair(final CallbackInfo ci) {
        if (!BlockManipulationClient.isPointingToPortal()) {
            return;
        }
        final HitResult hit = this.hitResult;
        if (!(hit instanceof BlockHitResult)) {
            return;
        }
        // Minecraft implements MinecraftDuck via VS's MixinMinecraft on the same class.
        ((MinecraftDuck) (Object) this).vs$setOriginalCrosshairTarget(hit);
    }
}
