package org.omnifix.mixin.perf;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.DataFixer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.omnifix.duck.IDeferrableIntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

/**
 * While the client is still applying join-time configuration packets, treat the integrated server
 * as paused for full world ticks (still tick connection once after the first super tick).
 */
@Mixin(IntegratedServer.class)
public abstract class IntegratedServerSuspendMixin extends MinecraftServer
        implements IDeferrableIntegratedServer {

    @Shadow
    private boolean paused;

    @Unique
    private int omnifix$numTickServerCalls;

    @Unique
    private final AtomicBoolean omnifix$hasPrimaryClientJoined = new AtomicBoolean(false);

    public IntegratedServerSuspendMixin(
            Thread serverThread,
            LevelStorageSource.LevelStorageAccess storageSource,
            PackRepository packRepository,
            WorldStem worldStem,
            Proxy proxy,
            DataFixer fixerUpper,
            Services services,
            ChunkProgressListenerFactory progressListenerFactory
    ) {
        super(serverThread, storageSource, packRepository, worldStem, proxy, fixerUpper, services, progressListenerFactory);
    }

    @WrapOperation(
            method = "tickServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;isPaused()Z",
                    ordinal = 0
            )
    )
    private boolean omnifix$preventTicks(Minecraft instance, Operation<Boolean> original) {
        return !omnifix$hasPrimaryClientJoined.get() || original.call(instance);
    }

    @Inject(method = "tickServer", at = @At("HEAD"))
    private void omnifix$countTicks(CallbackInfo ci) {
        this.omnifix$numTickServerCalls++;
    }

    @WrapWithCondition(
            method = "tickServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;tickServer(Ljava/util/function/BooleanSupplier;)V",
                    ordinal = 0
            )
    )
    private boolean omnifix$preventRunningFullServerTick(
            MinecraftServer server,
            BooleanSupplier hasTimeLeft
    ) {
        if (this.omnifix$numTickServerCalls >= 2
                && this.paused
                && !omnifix$hasPrimaryClientJoined.get()) {
            var conn = this.getConnection();
            if (conn != null) {
                conn.tick();
            }
            return false;
        }
        return true;
    }

    @Override
    public void omnifix$markClientLoadFinished() {
        omnifix$hasPrimaryClientJoined.set(true);
    }
}
