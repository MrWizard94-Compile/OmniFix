package org.omnifix.forge.mixin;

import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.LoadingModList;
import org.omnifix.kernel.StackPolicyEngine;
import org.omnifix.kernel.feature.FeatureUnitRegistry;
import org.omnifix.kernel.feature.FeatureUnits;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Gates every OmniFix mixin on FeatureUnit config and the mods it needs. OmniFix declares no
 * mandatory mod dependencies — it is a universal compat layer — so each mixin must be dropped
 * here, before Mixin resolves its target class, in packs that lack the mods it stitches together
 * or when the player has disabled that FeatureUnit.
 *
 * <p>{@link org.omnifix.kernel.StackPolicyEngine} domain probes run later in the mod constructor,
 * so this plugin consults FML's {@link LoadingModList} directly (populated at mod discovery) and
 * early-loads {@code config/omnifix-features.properties} for FeatureUnit toggles.
 *
 * <p>Vanilla Mojira and ModernFix bugfix mixins require no extra mods (except shape-cache targets)
 * and are gated only by FeatureUnit config (+ optional mod presence for targeted caches).
 */
public class OmniFixMixinPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
        bootstrapEmbeddedMixinLibraries();
        FeatureUnits.registerBuiltins();
        try {
            FeatureUnitRegistry.loadConfig(FMLPaths.CONFIGDIR.get().resolve("omnifix-features.properties"));
        } catch (Throwable t) {
            // Config dir may be unavailable in odd environments; defaults still apply.
        }
        // Early Mixin NO_GROUP member leak patch (before inject registration piles up).
        if (FeatureUnitRegistry.isConfigEnabled(FeatureUnits.PERF_MIXIN_INJECTOR_GROUP)
                && !modLoading("modernfix")) {
            org.omnifix.mixin.MixinInjectorGroupPatch.apply();
        }
    }

    /**
     * MixinExtras and MixinSquared are shaded into the OmniFix jar (see omnifix-forge build).
     * Both require an explicit bootstrap before mixins that use {@code @WrapMethod}/{@code @WrapOperation}
     * or MixinSquared cancellers are applied.
     */
    private static void bootstrapEmbeddedMixinLibraries() {
        try {
            Class.forName("com.llamalad7.mixinextras.MixinExtrasBootstrap")
                    .getMethod("init")
                    .invoke(null);
        } catch (Throwable t) {
            // Another mod may already have initialized MixinExtras, or the shade failed — fail soft;
            // mixins that need MixinExtras will error loudly at apply time if the library is missing.
        }
        try {
            Class.forName("com.bawnorton.mixinsquared.MixinSquaredBootstrap")
                    .getMethod("init")
                    .invoke(null);
        } catch (Throwable t) {
            // Same: soft fail; frustum canceller path logs later if SPI is absent.
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String unit = featureUnitForMixin(mixinClassName);
        if (unit != null && !FeatureUnitRegistry.isConfigEnabled(unit)) {
            return false;
        }

        // Coexistence: skip ports already applied by peer fix mods to avoid double-mixin crashes.
        if (unit != null && unit.startsWith("FU-MF-") && modLoading("modernfix")) {
            return false;
        }
        if (unit != null && unit.startsWith("vanilla.") && modLoading("debugify")) {
            return false;
        }
        if (unit != null && unit.startsWith("leak.") && modLoading("alltheleaks")) {
            return false;
        }

        // Chat signing / narrator are policy features in vanilla package but not Debugify Mojira.
        if (mixinClassName.contains("ChatSigningOffMixin") || mixinClassName.contains("NarratorLinuxQuietMixin")) {
            // Do not skip when Debugify present (not Mojira double-apply).
            return unit == null || FeatureUnitRegistry.isConfigEnabled(unit);
        }

        // Optional-mod gated: Patchouli books / ResourcefulLib highlights.
        if (mixinClassName.contains("PatchouliBookDedupMixin") && !modLoading("patchouli")) {
            return false;
        }
        if (mixinClassName.contains("ResourcefulLibHighlightMixin")
                && !modLoading("resourcefullib")) {
            return false;
        }

        // Feature package (mcfunction profiling, registry progress).
        if (mixinClassName.contains("mixin.feature.") || mixinClassName.contains(".feature.")) {
            if (modLoading("modernfix")
                    && (mixinClassName.contains("GameDataRegistryProgressMixin")
                            || mixinClassName.contains("ServerFunctionManagerProfilingMixin"))) {
                return false;
            }
            return unit == null || FeatureUnitRegistry.isConfigEnabled(unit);
        }

        // Leak / network / measured perf packages: no VS/IP requirement.
        if (isLeakMixin(mixinClassName) || isNetMixin(mixinClassName) || isPerfMixin(mixinClassName)) {
            // Only skip MF-class ports that double-apply the same inject sites when MF is present.
            // Original OmniFix AI/entity/idle perf units must remain active alongside ModernFix.
            if (modLoading("modernfix") && isModernFixPerfPort(mixinClassName)) {
                return false;
            }
            return true;
        }

        // Vanilla Mojira + ModernFix bugfix packages: no VS/IP requirement.
        if (isVanillaMixin(mixinClassName) || isBugfixMixin(mixinClassName)) {
            // Shape-cache mixins target optional third-party classes.
            if (mixinClassName.contains("ShapeCacheRSMixin") && !modLoading("refinedstorage")) {
                return false;
            }
            if (mixinClassName.contains("ShapeCacheCyclicMixin") && !modLoading("cyclic")) {
                return false;
            }
            // Paper SortedArraySet fix is redundant (and can conflict) under Moonrise.
            if (mixinClassName.contains("SortedArraySetMixin") && modLoading("moonrise")) {
                return false;
            }
            // CoFH FlagManager only applies when CoFH Core is present.
            if (mixinClassName.contains("FlagManagerMixin") && !modLoading("cofh_core")) {
                return false;
            }
            // CTM ResourceUtil only when ConnectedTexturesMod is present.
            if (mixinClassName.contains("ResourceUtilMixin") && mixinClassName.contains("ctm")
                    && !modLoading("ctm")) {
                return false;
            }
            return true;
        }

        // Empty StateHolder table only useful without FerriteCore (FC already optimizes neighbours).
        if (mixinClassName.contains("StateHolderEmptyTableMixin") && modLoading("ferritecore")) {
            return false;
        }
        // FakeStateMap is only valuable with FerriteCore.
        if (mixinClassName.contains("StateDefinitionFakeMapMixin") && !modLoading("ferritecore")) {
            return false;
        }


        // Create RaycastHelper ship clip: needs Create + VS (Clockwork optional consumer).
        if (mixinClassName.contains("MixinCreateRaycastHelperShipClip")) {
            return modLoading("create") && modLoading("valkyrienskies");
        }

        // Create × Immersive Portals track mixins (no VS required).
        if (isCreatePortalMixin(mixinClassName)) {
            return modLoading("create") && modLoading("immersive_portals");
        }

        if (mixinClassName.contains("MixinRenderSectionManagerShipDataProbe")) {
            if (!StackPolicyEngine.isDiagnosticsEnabled()
                    && !FeatureUnitRegistry.isConfigEnabled(FeatureUnits.VP_DIAGNOSTICS)) {
                return false;
            }
        }

        // Every mixin in the valkyrienportals package stitches Valkyrien Skies to Immersive
        // Portals; without both there is neither a conflict to fix nor a target to hit.
        if (!modLoading("valkyrienskies") || !modLoading("immersive_portals")) {
            return false;
        }
        // The RenderSectionManager mixins additionally target the Embeddium/Rubidium renderer.
        if (mixinClassName.contains("MixinRenderSectionManager")
                && !modLoading("embeddium") && !modLoading("rubidium")) {
            return false;
        }
        return true;
    }

    /**
     * Maps a mixin simple/fully-qualified class name to its FeatureUnit id.
     * Returns null when the mixin is structural (e.g. invoker used by another gated mixin only).
     */
    private static String featureUnitForMixin(String mixinClassName) {
        // --- Vanilla Mojira ---
        if (mixinClassName.contains("SpectatorStuckEffects")
                || mixinClassName.contains("SpectatorGuiOverlay")
                || mixinClassName.contains("SpectatorNauseaOverlay")) {
            return FeatureUnits.VANILLA_SPECTATOR_STUCK;
        }
        if (mixinClassName.contains("SpectatorBedMixin")) {
            return FeatureUnits.VANILLA_SPECTATOR_BED;
        }
        if (mixinClassName.contains("MendingBreakProgressMixin")) {
            return FeatureUnits.VANILLA_MENDING_BREAK;
        }
        if (mixinClassName.contains("Sapling2x2Mixin")) {
            return FeatureUnits.VANILLA_SAPLING_2X2;
        }
        if (mixinClassName.contains("FullscreenStateMixin")) {
            return FeatureUnits.VANILLA_FULLSCREEN;
        }
        if (mixinClassName.contains("SpectatorBreakMixin")) {
            return FeatureUnits.VANILLA_SPECTATOR_BREAK;
        }
        if (mixinClassName.contains("SpectatorProjectile")) {
            return FeatureUnits.VANILLA_SPECTATOR_PROJECTILE;
        }
        if (mixinClassName.contains("SpChatSpamMixin")) {
            return FeatureUnits.VANILLA_SP_CHAT_SPAM;
        }
        if (mixinClassName.contains("RawCopperSoundMixin")) {
            return FeatureUnits.VANILLA_RAW_COPPER_SOUND;
        }
        if (mixinClassName.contains("CreeperDefuseMixin")) {
            return FeatureUnits.VANILLA_CREEPER_DEFUSE;
        }
        if (mixinClassName.contains("TitleClearMixin")) {
            return FeatureUnits.VANILLA_TITLE_CLEAR;
        }
        if (mixinClassName.contains("HotbarRespawnMixin")) {
            return FeatureUnits.VANILLA_HOTBAR_RESPAWN;
        }
        if (mixinClassName.contains("UseSlowAfterDropMixin")) {
            return FeatureUnits.VANILLA_USE_SLOW_AFTER_DROP;
        }
        if (mixinClassName.contains("BreakDelayDropToolMixin")) {
            return FeatureUnits.VANILLA_BREAK_DELAY_DROP_TOOL;
        }
        if (mixinClassName.contains("CrossbowOffhandMixin")) {
            return FeatureUnits.VANILLA_CROSSBOW_OFFHAND;
        }
        if (mixinClassName.contains("DragStackInvisibleMixin")) {
            return FeatureUnits.VANILLA_DRAG_STACK_INVISIBLE;
        }
        if (mixinClassName.contains("MouseInventoryMixin")) {
            return FeatureUnits.VANILLA_MOUSE_INVENTORY;
        }
        if (mixinClassName.contains("LeatherSkeletonStrayMixin")) {
            return FeatureUnits.VANILLA_LEATHER_SKELETON_STRAY;
        }
        if (mixinClassName.contains("ZombieVillagerJockeyMixin")) {
            return FeatureUnits.VANILLA_ZOMBVILLAGER_JOCKEY;
        }
        if (mixinClassName.contains("CreativeLadderSlowMixin")) {
            return FeatureUnits.VANILLA_CREATIVE_LADDER_SLOW;
        }
        if (mixinClassName.contains("DoubleSneakAnimMixin")) {
            return FeatureUnits.VANILLA_DOUBLE_SNEAK_ANIM;
        }
        if (mixinClassName.contains("F3DoubleMixin")) {
            return FeatureUnits.VANILLA_F3_DOUBLE;
        }
        if (mixinClassName.contains("SpectatorConsume")) {
            return FeatureUnits.VANILLA_SPECTATOR_CONSUME;
        }
        if (mixinClassName.contains("LightningDropsMixin")) {
            return FeatureUnits.VANILLA_LIGHTNING_DROPS;
        }
        if (mixinClassName.contains("ArmorStandDeathParticlesMixin")) {
            return FeatureUnits.VANILLA_ARMORSTAND_PARTICLES;
        }
        if (mixinClassName.contains("FishingKillCountMixin")) {
            return FeatureUnits.VANILLA_FISHING_KILL_COUNT;
        }
        if (mixinClassName.contains("PufferfishDyingMixin")) {
            return FeatureUnits.VANILLA_PUFFERFISH_DYING;
        }
        if (mixinClassName.contains("TelemetryDisable")) {
            return FeatureUnits.VANILLA_TELEMETRY_DISABLE;
        }
        if (mixinClassName.contains("ShieldHurtSoundMixin")) {
            return FeatureUnits.VANILLA_SHIELD_HURT_SOUND;
        }
        if (mixinClassName.contains("BoatSlimeHoverMixin")) {
            return FeatureUnits.VANILLA_BOAT_SLIME_HOVER;
        }
        if (mixinClassName.contains("OffhandRodPunchMixin")) {
            return FeatureUnits.VANILLA_OFFHAND_ROD_PUNCH;
        }
        if (mixinClassName.contains("RiptideOffhandMixin")) {
            return FeatureUnits.VANILLA_RIPTIDE_OFFHAND;
        }
        if (mixinClassName.contains("WolfHeartsMixin")) {
            return FeatureUnits.VANILLA_WOLF_HEARTS;
        }
        if (mixinClassName.contains("StriderSaddlePeacefulMixin")) {
            return FeatureUnits.VANILLA_STRIDER_SADDLE_PEACEFUL;
        }
        if (mixinClassName.contains("PeacefulSaturationMixin")) {
            return FeatureUnits.VANILLA_PEACEFUL_SATURATION;
        }
        if (mixinClassName.contains("CmdMinecartNbtMixin")) {
            return FeatureUnits.VANILLA_CMD_MINECART_NBT;
        }
        if (mixinClassName.contains("UnknownPassengerMixin")) {
            return FeatureUnits.VANILLA_UNKNOWN_PASSENGER;
        }
        if (mixinClassName.contains("CtrlQCraftMixin")) {
            return FeatureUnits.VANILLA_CTRL_Q_CRAFT;
        }
        if (mixinClassName.contains("XpBarVanishMixin")) {
            return FeatureUnits.VANILLA_XP_BAR_VANISH;
        }
        if (mixinClassName.contains("HighSpeedFlickerMixin")) {
            return FeatureUnits.VANILLA_HIGH_SPEED_FLICKER;
        }
        if (mixinClassName.contains("ArmorStandDarkMixin")) {
            return FeatureUnits.VANILLA_ARMORSTAND_DARK;
        }
        if (mixinClassName.contains("DragonVoidPortalMixin")) {
            return FeatureUnits.VANILLA_DRAGON_VOID_PORTAL;
        }
        if (mixinClassName.contains("DimTeleportStateMixin")) {
            return FeatureUnits.VANILLA_DIM_TELEPORT_STATE;
        }
        if (mixinClassName.contains("GroupAiDeathMixin")) {
            return FeatureUnits.VANILLA_GROUP_AI_DEATH;
        }
        if (mixinClassName.contains("EntityAnimFreezeMixin")) {
            return FeatureUnits.VANILLA_ENTITY_ANIM_FREEZE;
        }
        if (mixinClassName.contains("FishingLineCrouchMixin")) {
            return FeatureUnits.VANILLA_FISHING_LINE_CROUCH;
        }
        if (mixinClassName.contains("PottableStatMixin")) {
            return FeatureUnits.VANILLA_POTTABLE_STAT;
        }
        if (mixinClassName.contains("SkeletonLookMixin")) {
            return FeatureUnits.VANILLA_SKELETON_LOOK;
        }
        if (mixinClassName.contains("EndRodCactusMixin")) {
            return FeatureUnits.VANILLA_ENDROD_CACTUS;
        }
        if (mixinClassName.contains("DrownBubblesMixin")) {
            return FeatureUnits.VANILLA_DROWN_BUBBLES;
        }

        // --- ModernFix bugfix ports ---
        if (mixinClassName.contains("RenderBuffersMixin") || mixinClassName.contains("BufferBuilderMixin")) {
            return FeatureUnits.MF_BUF_LEAK;
        }
        if (mixinClassName.contains("ResourceUtilMixin") && mixinClassName.contains("ctm")) {
            return FeatureUnits.MF_CTM_CME;
        }
        if (mixinClassName.contains("TelemetryDisable")) {
            return FeatureUnits.VANILLA_TELEMETRY_DISABLE;
        }
        if (mixinClassName.contains("MixinCreateRaycastHelperShipClip")) {
            return FeatureUnits.VS_CLOCKWORK_CLIP;
        }
        if (mixinClassName.contains("PotentialSpawnsMixin")
                || mixinClassName.contains("ForgeEventFactoryPotentialSpawnsMixin")) {
            return FeatureUnits.PERF_POTENTIAL_SPAWNS;
        }
        if (mixinClassName.contains("RecipeManagerReloadLogMixin")) {
            return FeatureUnits.PERF_RECIPE_RELOAD_LOG;
        }
        if (mixinClassName.contains("RegistryObjectGetMixin")) {
            return FeatureUnits.PERF_REGISTRY_OBJECT;
        }
        if (mixinClassName.contains("UtilThreadPriorityMixin")
                || mixinClassName.contains("IntegratedServerPriorityMixin")) {
            return FeatureUnits.PERF_THREAD_PRIORITY;
        }
        if (mixinClassName.contains("ChatSigningOffMixin")
                || mixinClassName.contains("ProfileKeyPairOffMixin")) {
            return FeatureUnits.FEATURE_CHAT_SIGNING_OFF;
        }
        if (mixinClassName.contains("DebugOverlayClearMixin")) {
            return FeatureUnits.BUGFIX_DEBUG_OVERLAY_CLEAR;
        }
        if (mixinClassName.contains("LootDataManagerFasterMixin")
                || mixinClassName.contains("ForgeHooksLootDeserializerMixin")) {
            return FeatureUnits.PERF_FASTER_LOOT;
        }
        if (mixinClassName.contains("StitcherStbMixin")) {
            return FeatureUnits.PERF_FASTER_TEXTURE_STITCH;
        }
        if (mixinClassName.contains("PatchouliBookDedupMixin")) {
            return FeatureUnits.PERF_PATCHOULI_BOOKS;
        }
        if (mixinClassName.contains("ServerFunctionManagerProfilingMixin")) {
            return FeatureUnits.FEATURE_MCFUNCTION_PROFILING;
        }
        if (mixinClassName.contains("GameDataRegistryProgressMixin")) {
            return FeatureUnits.FEATURE_REGISTRY_PROGRESS;
        }
        if (mixinClassName.contains("MobFenceEscapeMixin")) {
            return FeatureUnits.VANILLA_MOB_FENCE_ESCAPE;
        }
        if (mixinClassName.contains("SlowFallParticlesMixin")) {
            return FeatureUnits.VANILLA_SLOW_FALL_PARTICLES;
        }
        if (mixinClassName.contains("PartialChunkSaveMixin")) {
            return FeatureUnits.VANILLA_PARTIAL_CHUNK_SAVE;
        }
        if (mixinClassName.contains("PistonReloadBeOrderMixin")) {
            return FeatureUnits.VANILLA_PISTON_RELOAD;
        }
        if (mixinClassName.contains("ResourcefulLibHighlightMixin")) {
            return FeatureUnits.PERF_RESOURCEFULLIB_HIGHLIGHTS;
        }
        if (mixinClassName.contains("HopperEntityCacheMixin")) {
            return FeatureUnits.PERF_HOPPER_ENTITY_CACHE;
        }
        if (mixinClassName.contains("OmniFixBrandingMixin")) {
            return FeatureUnits.FEATURE_OMNIFIX_BRANDING;
        }
        if (mixinClassName.contains("ClientEntityCollisionMixin")) {
            return FeatureUnits.PERF_CLIENT_ENTITY_COLLISION;
        }
        if (mixinClassName.contains("DirectionValuesCacheMixin")) {
            return FeatureUnits.PERF_DIRECTION_VALUES_CACHE;
        }
        if (mixinClassName.contains("ItemEntityMergeCacheMixin")) {
            return FeatureUnits.PERF_ITEM_ENTITY_MERGE_CACHE;
        }
        if (mixinClassName.contains("AdvancementReloadLogMixin")) {
            return FeatureUnits.PERF_ADVANCEMENT_RELOAD_LOG;
        }
        if (mixinClassName.contains("ForceCloseLoadingScreenMixin")) {
            return FeatureUnits.FEATURE_FORCE_CLOSE_LOADING;
        }
        if (mixinClassName.contains("PathRecalcThrottleMixin")) {
            return FeatureUnits.PERF_PATH_RECALC_THROTTLE;
        }
        if (mixinClassName.contains("UuidDuplicateLogMixin")) {
            return FeatureUnits.BUGFIX_UUID_LOG_SPAM;
        }
        if (mixinClassName.contains("SkipEmptyRandomTickMixin")) {
            return FeatureUnits.PERF_SKIP_EMPTY_RANDOM_TICK;
        }
        if (mixinClassName.contains("ExperienceOrbScanCacheMixin")) {
            return FeatureUnits.PERF_XP_ORB_SCAN_CACHE;
        }
        if (mixinClassName.contains("GoalSelectorRateMixin")) {
            return FeatureUnits.PERF_GOAL_SELECTOR_RATE;
        }
        if (mixinClassName.contains("ParticleEngineEmptyTickMixin")) {
            return FeatureUnits.PERF_PARTICLE_EMPTY_TICK;
        }
        if (mixinClassName.contains("EmptyBlockEntityTickMixin")) {
            return FeatureUnits.PERF_EMPTY_BE_TICK;
        }
        if (mixinClassName.contains("EmptyBlockDropsMixin")) {
            return FeatureUnits.PERF_EMPTY_BLOCK_DROPS;
        }
        if (mixinClassName.contains("ClassInstanceMultiMapMixin")) {
            return FeatureUnits.PERF_ENTITY_SECTION_MAP;
        }
        if (mixinClassName.contains("BrewingStandIdleMixin")) {
            return FeatureUnits.PERF_BREWING_STAND_IDLE;
        }
        if (mixinClassName.contains("SensorScanRateMixin")) {
            return FeatureUnits.PERF_SENSOR_SCAN_RATE;
        }
        if (mixinClassName.contains("ArrowInGroundThrottleMixin")) {
            return FeatureUnits.PERF_ARROW_INGROUND;
        }
        if (mixinClassName.contains("FurnaceIdleMixin")) {
            return FeatureUnits.PERF_FURNACE_IDLE;
        }
        if (mixinClassName.contains("CampfireIdleMixin")) {
            return FeatureUnits.PERF_CAMPFIRE_IDLE;
        }
        if (mixinClassName.contains("TargetGoalIntervalMixin")) {
            return FeatureUnits.PERF_TARGET_GOAL_INTERVAL;
        }
        if (mixinClassName.contains("DirectionGetNearestCacheMixin")) {
            return FeatureUnits.PERF_DIRECTION_GET_NEAREST;
        }
        if (mixinClassName.contains("BeehiveEmptyMixin")) {
            return FeatureUnits.PERF_BEEHIVE_EMPTY;
        }
        if (mixinClassName.contains("JukeboxIdleMixin")) {
            return FeatureUnits.PERF_JUKEBOX_IDLE;
        }
        if (mixinClassName.contains("EmptyEffectsTickMixin")) {
            return FeatureUnits.PERF_EMPTY_EFFECTS_TICK;
        }
        if (mixinClassName.contains("EffectsOpenHashMapMixin")) {
            return FeatureUnits.PERF_EFFECTS_MAP;
        }
        if (mixinClassName.contains("BellIdleMixin")) {
            return FeatureUnits.PERF_BELL_IDLE;
        }
        if (mixinClassName.contains("ShulkerBoxIdleMixin")) {
            return FeatureUnits.PERF_SHULKER_BOX_IDLE;
        }
        if (mixinClassName.contains("ChestLidIdleMixin")
                || mixinClassName.contains("ChestLidControllerAccessor")) {
            return FeatureUnits.PERF_CHEST_LID_IDLE;
        }
        if (mixinClassName.contains("SignEditIdleMixin")) {
            return FeatureUnits.PERF_SIGN_EDIT_IDLE;
        }
        if (mixinClassName.contains("EnchantmentTableIdleMixin")) {
            return FeatureUnits.PERF_ENCHANT_TABLE_IDLE;
        }
        if (mixinClassName.contains("AvoidEntityScanThrottleMixin")) {
            return FeatureUnits.PERF_AVOID_ENTITY_THROTTLE;
        }
        if (mixinClassName.contains("RandomStrollIntervalMixin")) {
            return FeatureUnits.PERF_RANDOM_STROLL_INTERVAL;
        }
        if (mixinClassName.contains("SpawnerNearCacheMixin")) {
            return FeatureUnits.PERF_SPAWNER_NEAR_CACHE;
        }
        if (mixinClassName.contains("SculkCatalystIdleMixin")) {
            return FeatureUnits.PERF_SCULK_CATALYST_IDLE;
        }
        if (mixinClassName.contains("FollowParentThrottleMixin")) {
            return FeatureUnits.PERF_FOLLOW_PARENT_THROTTLE;
        }
        if (mixinClassName.contains("TemptGoalThrottleMixin")) {
            return FeatureUnits.PERF_TEMPT_GOAL_THROTTLE;
        }
        if (mixinClassName.contains("BreedGoalThrottleMixin")) {
            return FeatureUnits.PERF_BREED_GOAL_THROTTLE;
        }
        if (mixinClassName.contains("MinecartHopperEmptyCacheMixin")) {
            return FeatureUnits.PERF_MINECART_HOPPER_CACHE;
        }
        if (mixinClassName.contains("BegGoalThrottleMixin")) {
            return FeatureUnits.PERF_BEG_GOAL_THROTTLE;
        }
        if (mixinClassName.contains("ConduitInactiveThrottleMixin")) {
            return FeatureUnits.PERF_CONDUIT_INACTIVE;
        }
        if (mixinClassName.contains("HangingEntitySurviveIntervalMixin")) {
            return FeatureUnits.PERF_HANGING_SURVIVE;
        }
        if (mixinClassName.contains("FollowOwnerRepathMixin")) {
            return FeatureUnits.PERF_FOLLOW_OWNER_REPATH;
        }
        if (mixinClassName.contains("ContainerOpenersRecheckMixin")) {
            return FeatureUnits.PERF_OPENERS_RECHECK;
        }
        if (mixinClassName.contains("MoveThroughVillageThrottleMixin")) {
            return FeatureUnits.PERF_MOVE_VILLAGE_THROTTLE;
        }
        if (mixinClassName.contains("FleeSunThrottleMixin")) {
            return FeatureUnits.PERF_FLEE_SUN_THROTTLE;
        }
        if (mixinClassName.contains("RestrictSunThrottleMixin")) {
            return FeatureUnits.PERF_RESTRICT_SUN_THROTTLE;
        }
        if (mixinClassName.contains("MoveToBlockIntervalMixin")) {
            return FeatureUnits.PERF_MOVE_TO_BLOCK_INTERVAL;
        }
        if (mixinClassName.contains("StrollVillageIntervalMixin")) {
            return FeatureUnits.PERF_STROLL_VILLAGE_INTERVAL;
        }
        if (mixinClassName.contains("XpOrbScanPeriodMixin")) {
            return FeatureUnits.PERF_XP_ORB_SCAN_PERIOD;
        }
        if (mixinClassName.contains("RemoveBlockThrottleMixin")) {
            return FeatureUnits.PERF_REMOVE_BLOCK_THROTTLE;
        }
        if (mixinClassName.contains("LeapAtTargetThrottleMixin")) {
            return FeatureUnits.PERF_LEAP_TARGET_THROTTLE;
        }
        if (mixinClassName.contains("EndGatewayEntityScanMixin")) {
            return FeatureUnits.PERF_END_GATEWAY_SCAN;
        }
        if (mixinClassName.contains("FollowMobThrottleMixin")) {
            return FeatureUnits.PERF_FOLLOW_MOB_THROTTLE;
        }
        if (mixinClassName.contains("FollowMobRepathMixin")) {
            return FeatureUnits.PERF_FOLLOW_MOB_REPATH;
        }
        if (mixinClassName.contains("DefendVillageThrottleMixin")) {
            return FeatureUnits.PERF_DEFEND_VILLAGE_THROTTLE;
        }
        if (mixinClassName.contains("OfferFlowerThrottleMixin")) {
            return FeatureUnits.PERF_OFFER_FLOWER_THROTTLE;
        }
        if (mixinClassName.contains("RunAroundCrazyThrottleMixin")) {
            return FeatureUnits.PERF_RUN_CRAZY_THROTTLE;
        }
        if (mixinClassName.contains("LookAtPlayerProbabilityMixin")) {
            return FeatureUnits.PERF_LOOK_AT_PROBABILITY;
        }
        if (mixinClassName.contains("FollowBoatThrottleMixin")) {
            return FeatureUnits.PERF_FOLLOW_BOAT_THROTTLE;
        }
        if (mixinClassName.contains("LandShoulderThrottleMixin")) {
            return FeatureUnits.PERF_LAND_SHOULDER_THROTTLE;
        }
        if (mixinClassName.contains("ResetAngerThrottleMixin")) {
            return FeatureUnits.PERF_RESET_ANGER_THROTTLE;
        }
        if (mixinClassName.contains("CatSitThrottleMixin")) {
            return FeatureUnits.PERF_CAT_SIT_THROTTLE;
        }
        if (mixinClassName.contains("CatLieThrottleMixin")) {
            return FeatureUnits.PERF_CAT_LIE_THROTTLE;
        }
        if (mixinClassName.contains("PanicGoalThrottleMixin")) {
            return FeatureUnits.PERF_PANIC_THROTTLE;
        }
        if (mixinClassName.contains("TradeWithPlayerThrottleMixin")) {
            return FeatureUnits.PERF_TRADE_PLAYER_THROTTLE;
        }
        if (mixinClassName.contains("DolphinJumpIntervalMixin")) {
            return FeatureUnits.PERF_DOLPHIN_JUMP_INTERVAL;
        }
        if (mixinClassName.contains("EatBlockThrottleMixin")) {
            return FeatureUnits.PERF_EAT_BLOCK_THROTTLE;
        }
        if (mixinClassName.contains("ClimbPowderSnowThrottleMixin")) {
            return FeatureUnits.PERF_CLIMB_POWDER_THROTTLE;
        }
        if (mixinClassName.contains("MoveRestrictionThrottleMixin")) {
            return FeatureUnits.PERF_MOVE_RESTRICTION_THROTTLE;
        }
        if (mixinClassName.contains("MoveTowardsTargetThrottleMixin")) {
            return FeatureUnits.PERF_MOVE_TARGET_THROTTLE;
        }
        if (mixinClassName.contains("RangedAttackIntervalMixin")) {
            return FeatureUnits.PERF_RANGED_ATTACK_INTERVAL;
        }
        if (mixinClassName.contains("RangedBowIntervalMixin")) {
            return FeatureUnits.PERF_RANGED_BOW_INTERVAL;
        }
        if (mixinClassName.contains("PathfindToRaidThrottleMixin")) {
            return FeatureUnits.PERF_PATHFIND_RAID_THROTTLE;
        }
        if (mixinClassName.contains("BreakDoorThrottleMixin")) {
            return FeatureUnits.PERF_BREAK_DOOR_THROTTLE;
        }
        if (mixinClassName.contains("FloatGoalThrottleMixin")) {
            return FeatureUnits.PERF_FLOAT_GOAL_THROTTLE;
        }
        if (mixinClassName.contains("MeleeCanUseCooldownMixin")) {
            return FeatureUnits.PERF_MELEE_CANUSE_COOLDOWN;
        }
        if (mixinClassName.contains("RangedCrossbowDelayMixin")) {
            return FeatureUnits.PERF_RANGED_CROSSBOW_DELAY;
        }
        if (mixinClassName.contains("DoorInteractThrottleMixin")) {
            return FeatureUnits.PERF_DOOR_INTERACT_THROTTLE;
        }
        if (mixinClassName.contains("RandomLookProbabilityMixin")) {
            return FeatureUnits.PERF_RANDOM_LOOK_PROBABILITY;
        }
        if (mixinClassName.contains("LlamaCaravanThrottleMixin")) {
            return FeatureUnits.PERF_LLAMA_CARAVAN_THROTTLE;
        }
        if (mixinClassName.contains("FollowFlockRepathMixin")) {
            return FeatureUnits.PERF_FOLLOW_FLOCK_REPATH;
        }
        if (mixinClassName.contains("AreaEffectCloudScanMixin")) {
            return FeatureUnits.PERF_AEC_SCAN_PERIOD;
        }
        if (mixinClassName.contains("TryFindWaterThrottleMixin")) {
            return FeatureUnits.PERF_TRY_FIND_WATER_THROTTLE;
        }
        if (mixinClassName.contains("OcelotAttackRepathMixin")) {
            return FeatureUnits.PERF_OCELOT_ATTACK_REPATH;
        }
        if (mixinClassName.contains("MoveBackToVillageThrottleMixin")) {
            return FeatureUnits.PERF_MOVE_BACK_VILLAGE_THROTTLE;
        }
        if (mixinClassName.contains("RandomStandThrottleMixin")) {
            return FeatureUnits.PERF_RANDOM_STAND_THROTTLE;
        }
        if (mixinClassName.contains("UseItemThrottleMixin")) {
            return FeatureUnits.PERF_USE_ITEM_THROTTLE;
        }
        if (mixinClassName.contains("SwellGoalThrottleMixin")) {
            return FeatureUnits.PERF_SWELL_GOAL_THROTTLE;
        }
        if (mixinClassName.contains("ItemMergePeriodMixin")) {
            return FeatureUnits.PERF_ITEM_MERGE_PERIOD;
        }
        if (mixinClassName.contains("BreathAirPathThrottleMixin")) {
            return FeatureUnits.PERF_BREATH_AIR_PATH;
        }
        if (mixinClassName.contains("SitWhenOrderedThrottleMixin")) {
            return FeatureUnits.PERF_SIT_ORDERED_THROTTLE;
        }
        if (mixinClassName.contains("NearestItemSensorRangeMixin")) {
            return FeatureUnits.PERF_NEAREST_ITEM_RANGE;
        }
        if (mixinClassName.contains("SecondaryPoiRadiusMixin")) {
            return FeatureUnits.PERF_SECONDARY_POI_RADIUS;
        }
        if (mixinClassName.contains("ArmorStandMarkerPushMixin")) {
            return FeatureUnits.PERF_ARMOR_STAND_MARKER_PUSH;
        }
        if (mixinClassName.contains("ShulkerAttachThrottleMixin")) {
            return FeatureUnits.PERF_SHULKER_ATTACH_THROTTLE;
        }
        if (mixinClassName.contains("MeleePathRecalcMixin")) {
            return FeatureUnits.PERF_MELEE_PATH_RECALC;
        }
        if (mixinClassName.contains("ItemStillPhysicsMixin")) {
            return FeatureUnits.PERF_ITEM_STILL_PHYSICS;
        }
        if (mixinClassName.contains("LivingPushThrottleMixin")) {
            return FeatureUnits.PERF_LIVING_PUSH_THROTTLE;
        }
        if (mixinClassName.contains("NearestLivingSensorRadiusMixin")) {
            return FeatureUnits.PERF_NEAREST_LIVING_RADIUS;
        }
        if (mixinClassName.contains("PlayerSensorRangeMixin")) {
            return FeatureUnits.PERF_PLAYER_SENSOR_RANGE;
        }
        if (mixinClassName.contains("NearestBedScanMixin")) {
            return FeatureUnits.PERF_NEAREST_BED_SCAN;
        }
        if (mixinClassName.contains("HurtByAlertYMixin")) {
            return FeatureUnits.PERF_HURT_ALERT_Y;
        }
        if (mixinClassName.contains("TargetSearchYMixin")) {
            return FeatureUnits.PERF_TARGET_SEARCH_Y;
        }
        if (mixinClassName.contains("FlyingHoverRadiusMixin")) {
            return FeatureUnits.PERF_FLYING_HOVER_RADIUS;
        }
        if (mixinClassName.contains("MinecartPushThrottleMixin")) {
            return FeatureUnits.PERF_MINECART_PUSH_THROTTLE;
        }
        if (mixinClassName.contains("BeePollinateCooldownMixin")) {
            return FeatureUnits.PERF_BEE_POLLINATE_COOLDOWN;
        }
        if (mixinClassName.contains("TemptingSensorRangeMixin")) {
            return FeatureUnits.PERF_TEMPTING_SENSOR_RANGE;
        }
        if (mixinClassName.contains("BeeHiveLocateMixin")) {
            return FeatureUnits.PERF_BEE_HIVE_LOCATE;
        }
        if (mixinClassName.contains("PhantomPlayerScanMixin")) {
            return FeatureUnits.PERF_PHANTOM_SCAN;
        }
        if (mixinClassName.contains("OwnerHurtByThrottleMixin")) {
            return FeatureUnits.PERF_OWNER_HURT_BY_THROTTLE;
        }
        if (mixinClassName.contains("OwnerHurtTargetThrottleMixin")) {
            return FeatureUnits.PERF_OWNER_HURT_TARGET_THROTTLE;
        }
        if (mixinClassName.contains("WaterAvoidStrollRadiusMixin")) {
            return FeatureUnits.PERF_WATER_AVOID_STROLL_RADIUS;
        }
        if (mixinClassName.contains("HealableRaiderCooldownMixin")) {
            return FeatureUnits.PERF_HEALABLE_RAIDER_COOLDOWN;
        }
        if (mixinClassName.contains("EndermanTakeIntervalMixin")) {
            return FeatureUnits.PERF_ENDERMAN_TAKE_INTERVAL;
        }
        if (mixinClassName.contains("WardenSensorRadiusMixin")) {
            return FeatureUnits.PERF_WARDEN_SENSOR_RADIUS;
        }
        if (mixinClassName.contains("AxolotlAttackRangeMixin")) {
            return FeatureUnits.PERF_AXOLOTL_ATTACK_RANGE;
        }
        if (mixinClassName.contains("FrogAttackRangeMixin")) {
            return FeatureUnits.PERF_FROG_ATTACK_RANGE;
        }
        if (mixinClassName.contains("EndermanLeaveIntervalMixin")) {
            return FeatureUnits.PERF_ENDERMAN_LEAVE_INTERVAL;
        }
        if (mixinClassName.contains("GhastWanderRadiusMixin")) {
            return FeatureUnits.PERF_GHAST_WANDER_RADIUS;
        }
        if (mixinClassName.contains("GhastFireballChargeMixin")) {
            return FeatureUnits.PERF_GHAST_FIREBALL_CHARGE;
        }
        if (mixinClassName.contains("SilverfishWakeScanMixin")) {
            return FeatureUnits.PERF_SILVERFISH_WAKE_SCAN;
        }
        if (mixinClassName.contains("BeeWanderChanceMixin")) {
            return FeatureUnits.PERF_BEE_WANDER_CHANCE;
        }
        if (mixinClassName.contains("BlazeFireIntervalMixin")) {
            return FeatureUnits.PERF_BLAZE_FIRE_INTERVAL;
        }
        if (mixinClassName.contains("GuardianAttackDurationMixin")) {
            return FeatureUnits.PERF_GUARDIAN_ATTACK_DURATION;
        }
        if (mixinClassName.contains("VexRandomMoveChanceMixin")) {
            return FeatureUnits.PERF_VEX_RANDOM_MOVE;
        }
        if (mixinClassName.contains("VexChargeChanceMixin")) {
            return FeatureUnits.PERF_VEX_CHARGE_CHANCE;
        }
        if (mixinClassName.contains("HoglinRepellentRangeMixin")) {
            return FeatureUnits.PERF_HOGLIN_REPELLENT_RANGE;
        }
        if (mixinClassName.contains("PiglinRepellentRangeMixin")) {
            return FeatureUnits.PERF_PIGLIN_REPELLENT_RANGE;
        }
        if (mixinClassName.contains("AllayHealPeriodMixin")) {
            return FeatureUnits.PERF_ALLAY_HEAL_PERIOD;
        }
        if (mixinClassName.contains("RabbitRaidRangeMixin")) {
            return FeatureUnits.PERF_RABBIT_RAID_RANGE;
        }
        if (mixinClassName.contains("SlimeJumpDelayMixin")) {
            return FeatureUnits.PERF_SLIME_JUMP_DELAY;
        }
        if (mixinClassName.contains("ElderGuardianAttackMixin")) {
            return FeatureUnits.PERF_ELDER_GUARDIAN_ATTACK;
        }
        if (mixinClassName.contains("EvokerFangIntervalMixin")) {
            return FeatureUnits.PERF_EVOKER_FANG_INTERVAL;
        }
        if (mixinClassName.contains("EvokerSummonIntervalMixin")) {
            return FeatureUnits.PERF_EVOKER_SUMMON_INTERVAL;
        }
        if (mixinClassName.contains("EvokerWololoIntervalMixin")) {
            return FeatureUnits.PERF_EVOKER_WOLOLO_INTERVAL;
        }
        if (mixinClassName.contains("TurtleGoHomeChanceMixin")) {
            return FeatureUnits.PERF_TURTLE_GO_HOME_CHANCE;
        }
        if (mixinClassName.contains("TurtleLayEggDurationMixin")) {
            return FeatureUnits.PERF_TURTLE_LAY_EGG_DURATION;
        }
        if (mixinClassName.contains("BeeGrowCropIntervalMixin")) {
            return FeatureUnits.PERF_BEE_GROW_CROP_INTERVAL;
        }
        if (mixinClassName.contains("PandaRollChanceMixin")) {
            return FeatureUnits.PERF_PANDA_ROLL_CHANCE;
        }
        if (mixinClassName.contains("PandaSneezeChanceMixin")) {
            return FeatureUnits.PERF_PANDA_SNEEZE_CHANCE;
        }
        if (mixinClassName.contains("PolarBearCubScanMixin")) {
            return FeatureUnits.PERF_POLAR_BEAR_CUB_SCAN;
        }
        if (mixinClassName.contains("DrownedWaterSearchMixin")) {
            return FeatureUnits.PERF_DROWNED_WATER_SEARCH;
        }
        if (mixinClassName.contains("BeeGoHiveTimeoutMixin")) {
            return FeatureUnits.PERF_BEE_GO_HIVE_TIMEOUT;
        }
        if (mixinClassName.contains("BeeGoFlowerTimeoutMixin")) {
            return FeatureUnits.PERF_BEE_GO_FLOWER_TIMEOUT;
        }
        if (mixinClassName.contains("FishingOpenWaterScanMixin")) {
            return FeatureUnits.PERF_FISHING_OPEN_WATER_SCAN;
        }
        if (mixinClassName.contains("PandaSitItemScanMixin")) {
            return FeatureUnits.PERF_PANDA_SIT_ITEM_SCAN;
        }
        if (mixinClassName.contains("BootstrapEarlyMixin")) {
            // Multi-unit early bootstrap; each patch self-gates inside the mixin.
            return null;
        }
        if (mixinClassName.contains("NarratorLinuxQuietMixin")) {
            return FeatureUnits.FEATURE_NARRATOR_LINUX;
        }
        if (mixinClassName.contains("MacCtrlQDropMixin")) {
            return FeatureUnits.VANILLA_MAC_CTRL_Q;
        }
        if (mixinClassName.contains("LinuxChatT")) {
            return FeatureUnits.VANILLA_LINUX_CHAT_T;
        }
        if (mixinClassName.contains("MacSprintBreakMixin")) {
            return FeatureUnits.VANILLA_MAC_SPRINT_BREAK;
        }
        if (mixinClassName.contains("TagEntryIdCacheMixin")
                || mixinClassName.contains("TagOrElementLocationCacheMixin")) {
            return FeatureUnits.PERF_TAG_ID_CACHE;
        }
        if (mixinClassName.contains("WallBlockShapeDedupMixin")) {
            return FeatureUnits.PERF_WALL_SHAPE_DEDUP;
        }
        if (mixinClassName.contains("BlockStateEnumCacheMixin")) {
            return FeatureUnits.PERF_BLOCKSTATE_ENUM_CACHE;
        }
        if (mixinClassName.contains("CreativeModeTabMemoizeMixin")) {
            return FeatureUnits.PERF_CREATIVE_TAB;
        }
        if (mixinClassName.contains("PalettedContainerCompactMixin")) {
            return FeatureUnits.PERF_COMPACT_BIT_STORAGE;
        }
        if (mixinClassName.contains("StructureCheckFastMixin")
                || mixinClassName.contains("ServerLevelStructureCheckMixin")) {
            return FeatureUnits.PERF_STRUCTURE_LOCATE;
        }
        if (mixinClassName.contains("GameDataObjectHolderMixin")) {
            return FeatureUnits.PERF_OBJECT_HOLDER;
        }
        if (mixinClassName.contains("BiomeTemperatureMixin")) {
            return FeatureUnits.PERF_BIOME_TEMP;
        }
        if (mixinClassName.contains("BatHalloweenDateMixin")
                || mixinClassName.contains("ChunkAccessStructureRefsMixin")
                || mixinClassName.contains("ChunkHolderEitherMixin")) {
            return FeatureUnits.PERF_TICKING_CHUNK_ALLOC;
        }
        if (mixinClassName.contains("MappedRegistryGrowMixin")) {
            return FeatureUnits.PERF_REGISTRY_GROW;
        }
        if (mixinClassName.contains("StateHolderEmptyTableMixin")) {
            return FeatureUnits.PERF_STATE_EMPTY_TABLE;
        }
        if (mixinClassName.contains("LdLibDummyWorldMixin")) {
            return FeatureUnits.LEAK_LDLIB_DUMMYWORLD;
        }
        if (mixinClassName.contains("BooleanPropertyEqualsMixin")
                || mixinClassName.contains("PropertyInternMixin")
                || mixinClassName.contains("TransformationHashMixin")
                || mixinClassName.contains("SelectorPredicateCacheMixin")
                || mixinClassName.contains("MultiVariantResolveMixin")
                || mixinClassName.contains("perf.model.")) {
            return FeatureUnits.PERF_MODEL_OPTS;
        }
        if (mixinClassName.contains("ForgeRegistryBitCacheMixin")) {
            return FeatureUnits.PERF_FORGE_REG_BITS;
        }
        if (mixinClassName.contains("SkinManagerHashCacheMixin")) {
            return FeatureUnits.PERF_PROFILE_TEXTURE;
        }
        if (mixinClassName.contains("AttributeSupplierCompactMixin")
                || mixinClassName.contains("AttributeSupplierBuilderDedupMixin")) {
            return FeatureUnits.PERF_ATTRIBUTE_SUPPLIER;
        }
        if (mixinClassName.contains("CubeDefinitionDedupMixin")) {
            return FeatureUnits.PERF_COMPACT_ENTITY_MODELS;
        }
        if (mixinClassName.contains("ChunkGeneratorStrongholdCacheMixin")
                || mixinClassName.contains("ServerLevelStrongholdCacheMixin")
                || mixinClassName.contains("ConcentricRingsRadiusRejectMixin")) {
            return FeatureUnits.PERF_CACHE_STRONGHOLDS;
        }
        if (mixinClassName.contains("FilePackResourcesIndexMixin")) {
            return FeatureUnits.PERF_ZIP_PACK_INDEX;
        }
        if (mixinClassName.contains("MinecraftReloadExecutorMixin")
                || mixinClassName.contains("MinecraftServerReloadExecutorMixin")
                || mixinClassName.contains("CreateWorldScreenReloadExecutorMixin")
                || mixinClassName.contains("WorldOpenFlowsReloadExecutorMixin")) {
            return FeatureUnits.PERF_DEDICATED_RELOAD;
        }
        if (mixinClassName.contains("NamespacedWrapperFastDummyMixin")) {
            return FeatureUnits.PERF_FAST_FORGE_DUMMIES;
        }
        if (mixinClassName.contains("MaterialRuleListAllocMixin")
                || mixinClassName.contains("SurfaceSequenceRuleAllocMixin")
                || mixinClassName.contains("NoiseChunkWrapAllocMixin")
                || mixinClassName.contains("SurfaceRulesLazyConditionMixin")
                || mixinClassName.contains("SurfaceRulesContextAllocMixin")) {
            return FeatureUnits.PERF_WORLDGEN_ALLOC;
        }
        if (mixinClassName.contains("ChunkAccessImposterShareMixin")
                || mixinClassName.contains("ImposterProtoChunkCompactMixin")) {
            return FeatureUnits.PERF_COMPACT_IMPOSTER;
        }
        if (mixinClassName.contains("IngredientDedupMixin")
                || mixinClassName.contains("IngredientItemValueCopyMixin")) {
            return FeatureUnits.PERF_INGREDIENT_DEDUP;
        }
        if (mixinClassName.contains("LivingEntityCapOrderMixin")
                || mixinClassName.contains("AttachCapabilitiesCancelableMixin")) {
            return FeatureUnits.PERF_FORGE_CAP_RETRIEVAL;
        }
        if (mixinClassName.contains("IntegratedServerSuspendMixin")
                || mixinClassName.contains("ClientPacketListenerSuspendMixin")
                || mixinClassName.contains("PlayerListSuspendMixin")
                || mixinClassName.contains("MinecraftWorldLoadSleepMixin")) {
            return FeatureUnits.PERF_SUSPEND_INTEGRATED;
        }
        if (mixinClassName.contains("IntegratedServerPriorityMixin")) {
            return FeatureUnits.PERF_THREAD_PRIORITY;
        }
        if (mixinClassName.contains("GameRendererItemStateMixin")
                || mixinClassName.contains("ItemRendererFastGuiMixin")) {
            return FeatureUnits.PERF_FASTER_ITEM_RENDER;
        }
        if (mixinClassName.contains("MappedRegistryLifecycleMixin")
                || mixinClassName.contains("VanillaRegistriesMemoizeMixin")
                || mixinClassName.contains("BlockStateDataCompactMixin")) {
            return FeatureUnits.PERF_COMPACT_MOJANG_REG;
        }
        if (mixinClassName.contains("StructureManagerCacheMixin")) {
            return FeatureUnits.PERF_CACHE_STRUCTURES;
        }
        if (mixinClassName.contains("DataFixersLazyMixin")
                || mixinClassName.contains("DataFixTypesKickMixin")
                || mixinClassName.contains("BlockEntityTypeDfuSkipMixin")
                || mixinClassName.contains("EntityTypeBuilderDfuSkipMixin")) {
            return FeatureUnits.PERF_DYNAMIC_DFU;
        }
        if (mixinClassName.contains("MinecraftServerRemoveSpawnChunksMixin")
                || mixinClassName.contains("ServerLevelNoSpawnTicketMixin")
                || mixinClassName.contains("ServerPlayerSkipFudgeSpawnMixin")
                || mixinClassName.contains("PlayerListRemoveStartTicketMixin")
                || mixinClassName.contains("EntityPortalSpawnLoadMixin")
                || mixinClassName.contains("SortedArraySetNullGuardMixin")) {
            return FeatureUnits.PERF_REMOVE_SPAWN_CHUNKS;
        }
        if (mixinClassName.contains("PalettedContainerPaletteMixin")
                || mixinClassName.contains("SurfaceRulesContextBiomeSetMixin")
                || mixinClassName.contains("BiomeConditionSourceOptimizeMixin")
                || mixinClassName.contains("NoiseBasedChunkGeneratorBiomeScanMixin")
                || mixinClassName.contains("SurfaceSystemOptimizeMixin")
                || mixinClassName.contains("BiomeManagerAccessor")) {
            return FeatureUnits.PERF_OPTIMIZE_SURFACE;
        }
        if (mixinClassName.contains("BlockStateBaseLazyCacheMixin")
                || mixinClassName.contains("BlocksRebuildCacheMixin")
                || mixinClassName.contains("BlockCallbacksLazyCacheMixin")) {
            return FeatureUnits.PERF_REDUCE_BLOCKSTATE_CACHE;
        }
        if (mixinClassName.contains("PathPackResourcesCacheMixin")
                || mixinClassName.contains("MinecraftServerPackRepoMixin")) {
            return FeatureUnits.PERF_PATH_PACK_CACHE;
        }
        if (mixinClassName.contains("UnihexByteContentsCompactMixin")
                || mixinClassName.contains("UnihexShortContentsCompactMixin")
                || mixinClassName.contains("GlyphProviderTypeLazyMixin")) {
            return FeatureUnits.PERF_COMPRESS_UNIHEX;
        }
        if (mixinClassName.contains("BlockItemDelegateHolderMixin")
                || mixinClassName.contains("ForgeRegistryDelegateMixin")) {
            return FeatureUnits.PERF_FORGE_REG_ALLOC;
        }
        if (mixinClassName.contains("StateDefinitionFakeMapMixin")) {
            return FeatureUnits.PERF_FAKE_STATE_MAP;
        }
        if (mixinClassName.contains("ImposterProtoChunkBeGuardMixin")) {
            return FeatureUnits.PERF_IMPOSTER_BE_GUARD;
        }
        if (mixinClassName.contains("ChunkHolderReleaseProtoMixin")
                || mixinClassName.contains("ChunkMapReleaseProtoMixin")) {
            return FeatureUnits.PERF_RELEASE_PROTOCHUNKS;
        }
        if (mixinClassName.contains("SimpleReloadInstanceDebugMixin")
                || mixinClassName.contains("ProfiledReloadInstanceMixin")) {
            return FeatureUnits.PERF_DEBUG_RELOADER;
        }
        if (mixinClassName.contains("MinecraftMeasureTimeMixin")
                || mixinClassName.contains("ConnectScreenMeasureTimeMixin")
                || mixinClassName.contains("BootstrapTimingMixin")) {
            return FeatureUnits.PERF_MEASURE_TIME;
        }
        if (mixinClassName.contains("KeyMappingPrewarmMixin")) {
            return FeatureUnits.PERF_KEYMAP_PREWARM;
        }
        if (mixinClassName.contains("MinecraftServerTickTimeMixin")) {
            return FeatureUnits.PERF_INTEGRATED_WATCHDOG;
        }
        if (mixinClassName.contains("StructureRepositorySoftCacheMixin")) {
            return FeatureUnits.PERF_DYNAMIC_STRUCTURE;
        }
        if (mixinClassName.contains("ChunkRebuildFastIteratorMixin")) {
            return FeatureUnits.PERF_CHUNK_MESHING;
        }
        if (mixinClassName.contains("DebugLevelSourceStatesMixin")) {
            return FeatureUnits.PERF_DEBUG_LEVEL_STATES;
        }
        if (mixinClassName.contains("ClientLanguageDynamicMixin")) {
            return FeatureUnits.PERF_DYNAMIC_LANGUAGES;
        }
        if (mixinClassName.contains("IngredientFasterMixin")
                || mixinClassName.contains("ForgeHooksHasNoElementsMixin")
                || mixinClassName.contains("WorldLoaderReloadTrackMixin")
                || mixinClassName.contains("MinecraftServerReloadTrackMixin")) {
            return FeatureUnits.PERF_FASTER_INGREDIENTS;
        }
        if (mixinClassName.contains("SearchRegistryLazyMixin")) {
            return FeatureUnits.PERF_LAZY_SEARCH;
        }
        if (mixinClassName.contains("BlockColorsThreadSafetyMixin")
                || mixinClassName.contains("ItemColorsThreadSafetyMixin")
                || mixinClassName.contains("ItemPropertiesThreadSafetyMixin")
                || mixinClassName.contains("LivingEntityRendererLayerThreadMixin")) {
            return FeatureUnits.MF_CLIENT_MAP_SAFETY;
        }
        if (mixinClassName.contains("ChunkAccessBeThreadMixin")
                || mixinClassName.contains("LevelThreadAccessor")) {
            return FeatureUnits.MF_BE_THREAD;
        }
        if (mixinClassName.contains("CyclopsDynamicModelMixin")) {
            return FeatureUnits.LEAK_CYCLOPS;
        }
        if (mixinClassName.contains("EmiLootEntityStackMixin")) {
            return FeatureUnits.LEAK_EMI_LOOT;
        }
        if (mixinClassName.contains("LdLibModularUiMixin")) {
            return FeatureUnits.LEAK_LDLIB;
        }
        if (mixinClassName.contains("ChunkMapLoadMixin") && mixinClassName.contains("chunk_deadlock")) {
            return FeatureUnits.MF_CHUNK_DEADLOCK;
        }
        if (mixinClassName.contains("ServerChunkCache_CurrentLoadingMixin")
                || (mixinClassName.contains("chunk_deadlock") && mixinClassName.contains("EntityMixin"))) {
            return FeatureUnits.MF_CHUNK_DEADLOCK;
        }
        if (mixinClassName.contains("MappedRegistryMixin")
                || mixinClassName.contains("NamespacedWrapperMixin")
                || mixinClassName.contains("ForgeRegistryTagManagerMixin")
                || mixinClassName.contains("ReloadableResourceManagerMixin")) {
            return FeatureUnits.MF_CONC_REG;
        }
        if (mixinClassName.contains("EnderDragonRendererMixin")) {
            return FeatureUnits.MF_DRAGON_LEAK;
        }
        if (mixinClassName.contains("LivingEntityRendererMixin")
                || mixinClassName.contains("PlayerRendererMixin")
                || mixinClassName.contains("PoseStackAccessor")) {
            return FeatureUnits.MF_POSE_STACK;
        }
        if (mixinClassName.contains("CreateWorldScreenMixin")) {
            return FeatureUnits.MF_EXP_SCREEN;
        }
        if (mixinClassName.contains("ServerGamePacketListenerImplMixin")) {
            return FeatureUnits.MF_VEHICLE_PKT;
        }
        if (mixinClassName.contains("missing_block_entities") || mixinClassName.contains("LevelChunkMixin")) {
            // LevelChunkMixin is only used for missing BEs in this project.
            if (mixinClassName.contains("LevelChunkMixin") || mixinClassName.contains("missing_block_entities")) {
                return FeatureUnits.MF_MISS_BE;
            }
        }
        if (mixinClassName.contains("ModelDataManagerMixin")) {
            return FeatureUnits.MF_MODELDATA_CME;
        }
        if (mixinClassName.contains("SortedArraySetMixin")) {
            return FeatureUnits.MF_PAPER_CHUNK;
        }
        if (mixinClassName.contains("paper_chunk_patches.ChunkMapMixin")
                || (mixinClassName.contains("ChunkMapMixin") && mixinClassName.contains("paper_chunk"))) {
            return FeatureUnits.MF_PAPER_CHUNKMAP;
        }
        if (mixinClassName.contains("FlagManagerMixin") || mixinClassName.contains("cofh_core_crash")) {
            return FeatureUnits.MF_COFH_FLAGS;
        }
        if (mixinClassName.contains("RecipeBookSettingsMixin") || mixinClassName.contains("recipe_book_type_desync")) {
            return FeatureUnits.MF_RECIPE_BOOK;
        }
        if (mixinClassName.contains("RegistryOpsMemoizedMixin")) {
            return FeatureUnits.MF_REGOPS_CME;
        }
        if (mixinClassName.contains("LevelStorageSourceMixin") || mixinClassName.contains("removed_dimensions")) {
            return FeatureUnits.MF_REMOVED_DIM;
        }
        if (mixinClassName.contains("ShapeCacheRSMixin") || mixinClassName.contains("ShapeCacheCyclicMixin")) {
            return FeatureUnits.MF_SHAPE_CACHE;
        }
        if (mixinClassName.contains("world_leaks") || (mixinClassName.contains("MinecraftMixin") && mixinClassName.contains("bugfix"))) {
            return FeatureUnits.MF_WORLD_LEAK;
        }
        if (mixinClassName.contains("WorldSelectionListMixin")) {
            return FeatureUnits.MF_WORLD_SCREEN;
        }

        // --- Create × IP ---
        if (mixinClassName.contains("MixinTrackBlockEntityPortal")) {
            return FeatureUnits.CREATE_IP_TRACKS_B;
        }
        if (mixinClassName.contains("MixinCarriageContraptionEntityNoIpTeleport")
                || mixinClassName.contains("MixinPortalBlockTrainPassengers")
                || mixinClassName.contains("MixinDimensionalCarriageDismount")) {
            return FeatureUnits.CREATE_IP_TRAIN_TRANSIT;
        }

        // --- VS × IP ---
        if (mixinClassName.contains("MixinGameRendererPortalCamera")
                || mixinClassName.contains("LevelRendererPrepareCullFrustumInvoker")) {
            return FeatureUnits.VP_PORTAL_CAMERA;
        }
        if (mixinClassName.contains("MixinMinecraftCrossPortalInteract")
                || mixinClassName.contains("CrossPortalInteract")) {
            return FeatureUnits.VP_CROSS_PORTAL_INTERACT;
        }
        if (mixinClassName.contains("MixinRenderSectionManagerPortalFog")) {
            return FeatureUnits.VP_PORTAL_FOG;
        }
        if (mixinClassName.contains("MixinValkyrienSkiesModShipUnloadGuard")) {
            return FeatureUnits.VP_SHIP_UNLOAD_CCE;
        }
        if (mixinClassName.contains("MixinVsCoreChunkTrackerPortalDims")
                || mixinClassName.contains("MixinChunkMapCrossDimGuard")
                || mixinClassName.contains("MixinChunkManagementUntrackGuard")) {
            return FeatureUnits.VP_SHIP_VIS;
        }
        if (mixinClassName.contains("MixinEntityDragger")) {
            return FeatureUnits.VP_ENTITY_DRAG;
        }
        if (mixinClassName.contains("MixinRenderSectionManagerShipDataProbe")) {
            return FeatureUnits.VP_DIAGNOSTICS;
        }

        // --- Leaks / network ---
        if (mixinClassName.contains("FakePlayerServerStopMixin")) {
            return FeatureUnits.LEAK_FORGE_FAKEPLAYER;
        }
        if (mixinClassName.contains("ServerLoginTimeoutMixin")) {
            return FeatureUnits.NET_LOGIN_TIMEOUT;
        }
        if (mixinClassName.contains("ReadTimeoutHandlerMixin")) {
            return FeatureUnits.NET_READ_TIMEOUT;
        }
        if (mixinClassName.contains("CompressionDecoderSizeMixin")) {
            return FeatureUnits.NET_COMPRESSION_SIZE;
        }
        if (mixinClassName.contains("ServerGamePlayTimeoutMixin")) {
            return FeatureUnits.NET_PLAY_TIMEOUT;
        }
        if (mixinClassName.contains("ClientboundCustomPayloadSizeMixin")
                || mixinClassName.contains("ServerboundCustomPayloadSizeMixin")
                || mixinClassName.contains("ClientboundCustomQuerySizeMixin")
                || mixinClassName.contains("ServerboundCustomQuerySizeMixin")
                || mixinClassName.contains("PacketEncoderSizeMixin")
                || mixinClassName.contains("Varint21FrameSizeMixin")
                || mixinClassName.contains("Varint21LengthFieldPrependerSizeMixin")) {
            return FeatureUnits.NET_PAYLOAD_SPLIT;
        }
        if (mixinClassName.contains("HandshakeHandlerMixin")) {
            return FeatureUnits.PERF_HANDSHAKE;
        }
        if (mixinClassName.contains("MinecraftServerSpinWaitMixin")) {
            return FeatureUnits.PERF_SPIN_WAIT;
        }
        return null;
    }

    private static boolean isCreatePortalMixin(String mixinClassName) {
        return mixinClassName.contains("createportals.mixin")
                || mixinClassName.contains("MixinTrackBlockEntityPortal")
                || mixinClassName.contains("MixinCarriageContraptionEntityNoIpTeleport")
                || mixinClassName.contains("MixinPortalBlockTrainPassengers")
                || mixinClassName.contains("MixinDimensionalCarriageDismount");
    }

    private static boolean isPerfMixin(String mixinClassName) {
        return mixinClassName.contains("org.omnifix.mixin.perf")
                || mixinClassName.contains("HandshakeHandlerMixin")
                || mixinClassName.contains("MinecraftServerSpinWaitMixin")
                || mixinClassName.contains("PotentialSpawnsMixin")
                || mixinClassName.contains("ForgeEventFactoryPotentialSpawnsMixin")
                || mixinClassName.contains("RecipeManagerReloadLogMixin")
                || mixinClassName.contains("RegistryObjectGetMixin")
                || mixinClassName.contains("UtilThreadPriorityMixin")
                || mixinClassName.contains("TagEntryIdCacheMixin")
                || mixinClassName.contains("WallBlockShapeDedupMixin")
                || mixinClassName.contains("BlockStateEnumCacheMixin")
                || mixinClassName.contains("CreativeModeTabMemoizeMixin")
                || mixinClassName.contains("PalettedContainerCompactMixin")
                || mixinClassName.contains("StructureCheckFastMixin")
                || mixinClassName.contains("ServerLevelStructureCheckMixin")
                || mixinClassName.contains("GameDataObjectHolderMixin")
                || mixinClassName.contains("TagOrElementLocationCacheMixin")
                || mixinClassName.contains("BiomeTemperatureMixin")
                || mixinClassName.contains("BatHalloweenDateMixin")
                || mixinClassName.contains("ChunkAccessStructureRefsMixin")
                || mixinClassName.contains("ChunkHolderEitherMixin")
                || mixinClassName.contains("MappedRegistryGrowMixin")
                || mixinClassName.contains("StateHolderEmptyTableMixin")
                || mixinClassName.contains("ForgeRegistryBitCacheMixin")
                || mixinClassName.contains("BooleanPropertyEqualsMixin")
                || mixinClassName.contains("PropertyInternMixin")
                || mixinClassName.contains("TransformationHashMixin")
                || mixinClassName.contains("SelectorPredicateCacheMixin")
                || mixinClassName.contains("MultiVariantResolveMixin")
                || mixinClassName.contains("SkinManagerHashCacheMixin")
                || mixinClassName.contains("AttributeSupplierCompactMixin")
                || mixinClassName.contains("AttributeSupplierBuilderDedupMixin")
                || mixinClassName.contains("CubeDefinitionDedupMixin")
                || mixinClassName.contains("ChunkGeneratorStrongholdCacheMixin")
                || mixinClassName.contains("ServerLevelStrongholdCacheMixin")
                || mixinClassName.contains("ConcentricRingsRadiusRejectMixin")
                || mixinClassName.contains("FilePackResourcesIndexMixin")
                || mixinClassName.contains("MinecraftReloadExecutorMixin")
                || mixinClassName.contains("MinecraftServerReloadExecutorMixin")
                || mixinClassName.contains("CreateWorldScreenReloadExecutorMixin")
                || mixinClassName.contains("WorldOpenFlowsReloadExecutorMixin")
                || mixinClassName.contains("NamespacedWrapperFastDummyMixin")
                || mixinClassName.contains("MaterialRuleListAllocMixin")
                || mixinClassName.contains("SurfaceSequenceRuleAllocMixin")
                || mixinClassName.contains("NoiseChunkWrapAllocMixin")
                || mixinClassName.contains("SurfaceRulesLazyConditionMixin")
                || mixinClassName.contains("SurfaceRulesContextAllocMixin")
                || mixinClassName.contains("ChunkAccessImposterShareMixin")
                || mixinClassName.contains("ImposterProtoChunkCompactMixin")
                || mixinClassName.contains("IngredientDedupMixin")
                || mixinClassName.contains("IngredientItemValueCopyMixin")
                || mixinClassName.contains("LivingEntityCapOrderMixin")
                || mixinClassName.contains("AttachCapabilitiesCancelableMixin")
                || mixinClassName.contains("IntegratedServerSuspendMixin")
                || mixinClassName.contains("ClientPacketListenerSuspendMixin")
                || mixinClassName.contains("PlayerListSuspendMixin")
                || mixinClassName.contains("MinecraftWorldLoadSleepMixin")
                || mixinClassName.contains("IntegratedServerPriorityMixin")
                || mixinClassName.contains("GameRendererItemStateMixin")
                || mixinClassName.contains("ItemRendererFastGuiMixin")
                || mixinClassName.contains("MappedRegistryLifecycleMixin")
                || mixinClassName.contains("VanillaRegistriesMemoizeMixin")
                || mixinClassName.contains("BlockStateDataCompactMixin")
                || mixinClassName.contains("StructureManagerCacheMixin")
                || mixinClassName.contains("DataFixersLazyMixin")
                || mixinClassName.contains("DataFixTypesKickMixin")
                || mixinClassName.contains("BlockEntityTypeDfuSkipMixin")
                || mixinClassName.contains("EntityTypeBuilderDfuSkipMixin")
                || mixinClassName.contains("MinecraftServerRemoveSpawnChunksMixin")
                || mixinClassName.contains("ServerLevelNoSpawnTicketMixin")
                || mixinClassName.contains("ServerPlayerSkipFudgeSpawnMixin")
                || mixinClassName.contains("PlayerListRemoveStartTicketMixin")
                || mixinClassName.contains("EntityPortalSpawnLoadMixin")
                || mixinClassName.contains("SortedArraySetNullGuardMixin")
                || mixinClassName.contains("PalettedContainerPaletteMixin")
                || mixinClassName.contains("SurfaceRulesContextBiomeSetMixin")
                || mixinClassName.contains("BiomeConditionSourceOptimizeMixin")
                || mixinClassName.contains("NoiseBasedChunkGeneratorBiomeScanMixin")
                || mixinClassName.contains("SurfaceSystemOptimizeMixin")
                || mixinClassName.contains("BiomeManagerAccessor")
                || mixinClassName.contains("BlockStateBaseLazyCacheMixin")
                || mixinClassName.contains("BlocksRebuildCacheMixin")
                || mixinClassName.contains("BlockCallbacksLazyCacheMixin")
                || mixinClassName.contains("PathPackResourcesCacheMixin")
                || mixinClassName.contains("MinecraftServerPackRepoMixin")
                || mixinClassName.contains("UnihexByteContentsCompactMixin")
                || mixinClassName.contains("UnihexShortContentsCompactMixin")
                || mixinClassName.contains("StructureRepositorySoftCacheMixin")
                || mixinClassName.contains("ChunkRebuildFastIteratorMixin")
                || mixinClassName.contains("DebugLevelSourceStatesMixin")
                || mixinClassName.contains("ClientLanguageDynamicMixin")
                || mixinClassName.contains("IngredientFasterMixin")
                || mixinClassName.contains("ForgeHooksHasNoElementsMixin")
                || mixinClassName.contains("WorldLoaderReloadTrackMixin")
                || mixinClassName.contains("MinecraftServerReloadTrackMixin")
                || mixinClassName.contains("SearchRegistryLazyMixin")
                || mixinClassName.contains("GlyphProviderTypeLazyMixin")
                || mixinClassName.contains("BlockItemDelegateHolderMixin")
                || mixinClassName.contains("ForgeRegistryDelegateMixin")
                || mixinClassName.contains("StateDefinitionFakeMapMixin")
                || mixinClassName.contains("ImposterProtoChunkBeGuardMixin")
                || mixinClassName.contains("ChunkHolderReleaseProtoMixin")
                || mixinClassName.contains("ChunkMapReleaseProtoMixin")
                || mixinClassName.contains("SimpleReloadInstanceDebugMixin")
                || mixinClassName.contains("ProfiledReloadInstanceMixin")
                || mixinClassName.contains("MinecraftMeasureTimeMixin")
                || mixinClassName.contains("ConnectScreenMeasureTimeMixin")
                || mixinClassName.contains("BootstrapTimingMixin")
                || mixinClassName.contains("KeyMappingPrewarmMixin")
                || mixinClassName.contains("MinecraftServerTickTimeMixin");
    }

    /**
     * Perf units that clone ModernFix inject sites and must not double-apply when MF is loaded.
     * Does <em>not</em> include original OmniFix AI/entity/idle throttles.
     */
    private static boolean isModernFixPerfPort(String mixinClassName) {
        return mixinClassName.contains("HandshakeHandlerMixin")
                || mixinClassName.contains("MinecraftServerSpinWaitMixin");
    }

    private static boolean isLeakMixin(String mixinClassName) {
        return mixinClassName.contains("org.omnifix.mixin.leak")
                || mixinClassName.contains("FakePlayerServerStopMixin");
    }

    private static boolean isNetMixin(String mixinClassName) {
        return mixinClassName.contains("org.omnifix.mixin.net")
                || mixinClassName.contains("ServerLoginTimeoutMixin")
                || mixinClassName.contains("ReadTimeoutHandlerMixin")
                || mixinClassName.contains("CompressionDecoderSizeMixin")
                || mixinClassName.contains("ServerGamePlayTimeoutMixin")
                || mixinClassName.contains("ClientboundCustomPayloadSizeMixin")
                || mixinClassName.contains("ServerboundCustomPayloadSizeMixin")
                || mixinClassName.contains("ClientboundCustomQuerySizeMixin")
                || mixinClassName.contains("ServerboundCustomQuerySizeMixin")
                || mixinClassName.contains("PacketEncoderSizeMixin")
                || mixinClassName.contains("Varint21FrameSizeMixin")
                || mixinClassName.contains("Varint21LengthFieldPrependerSizeMixin");
    }

    private static boolean isVanillaMixin(String mixinClassName) {
        return mixinClassName.contains("org.omnifix.mixin.vanilla")
                || mixinClassName.contains("SpectatorStuckEffects")
                || mixinClassName.contains("SpectatorBedMixin")
                || mixinClassName.contains("SpectatorGuiOverlay")
                || mixinClassName.contains("SpectatorNauseaOverlay")
                || mixinClassName.contains("MendingBreakProgressMixin")
                || mixinClassName.contains("Sapling2x2Mixin")
                || mixinClassName.contains("FullscreenStateMixin")
                || mixinClassName.contains("SpectatorBreakMixin")
                || mixinClassName.contains("SpectatorProjectile")
                || mixinClassName.contains("SpChatSpamMixin")
                || mixinClassName.contains("RawCopperSoundMixin")
                || mixinClassName.contains("CreeperDefuseMixin")
                || mixinClassName.contains("TitleClearMixin")
                || mixinClassName.contains("HotbarRespawnMixin")
                || mixinClassName.contains("UseSlowAfterDropMixin")
                || mixinClassName.contains("BreakDelayDropToolMixin")
                || mixinClassName.contains("CrossbowOffhandMixin")
                || mixinClassName.contains("DragStackInvisibleMixin")
                || mixinClassName.contains("MouseInventoryMixin")
                || mixinClassName.contains("LeatherSkeletonStrayMixin")
                || mixinClassName.contains("ZombieVillagerJockeyMixin")
                || mixinClassName.contains("CreativeLadderSlowMixin")
                || mixinClassName.contains("DoubleSneakAnimMixin")
                || mixinClassName.contains("F3DoubleMixin")
                || mixinClassName.contains("SpectatorConsume")
                || mixinClassName.contains("LightningDropsMixin")
                || mixinClassName.contains("ArmorStandDeathParticlesMixin")
                || mixinClassName.contains("FishingKillCountMixin")
                || mixinClassName.contains("PufferfishDyingMixin")
                || mixinClassName.contains("ShieldHurtSoundMixin")
                || mixinClassName.contains("BoatSlimeHoverMixin")
                || mixinClassName.contains("OffhandRodPunchMixin")
                || mixinClassName.contains("RiptideOffhandMixin")
                || mixinClassName.contains("WolfHeartsMixin")
                || mixinClassName.contains("StriderSaddlePeacefulMixin")
                || mixinClassName.contains("PeacefulSaturationMixin")
                || mixinClassName.contains("CmdMinecartNbtMixin")
                || mixinClassName.contains("UnknownPassengerMixin")
                || mixinClassName.contains("CtrlQCraftMixin")
                || mixinClassName.contains("XpBarVanishMixin")
                || mixinClassName.contains("HighSpeedFlickerMixin")
                || mixinClassName.contains("ArmorStandDarkMixin")
                || mixinClassName.contains("DragonVoidPortalMixin")
                || mixinClassName.contains("DimTeleportStateMixin")
                || mixinClassName.contains("GroupAiDeathMixin")
                || mixinClassName.contains("EntityAnimFreezeMixin")
                || mixinClassName.contains("FishingLineCrouchMixin")
                || mixinClassName.contains("PottableStatMixin")
                || mixinClassName.contains("SkeletonLookMixin")
                || mixinClassName.contains("EndRodCactusMixin")
                || mixinClassName.contains("DrownBubblesMixin");
    }

    private static boolean isBugfixMixin(String mixinClassName) {
        return mixinClassName.contains("org.omnifix.mixin.bugfix")
                || mixinClassName.contains("RenderBuffersMixin")
                || mixinClassName.contains("ServerChunkCache_CurrentLoadingMixin")
                || mixinClassName.contains("MappedRegistryMixin")
                || mixinClassName.contains("NamespacedWrapperMixin")
                || mixinClassName.contains("ForgeRegistryTagManagerMixin")
                || mixinClassName.contains("ReloadableResourceManagerMixin")
                || mixinClassName.contains("EnderDragonRendererMixin")
                || mixinClassName.contains("LivingEntityRendererMixin")
                || mixinClassName.contains("PlayerRendererMixin")
                || mixinClassName.contains("PoseStackAccessor")
                || mixinClassName.contains("CreateWorldScreenMixin")
                || mixinClassName.contains("ServerGamePacketListenerImplMixin")
                || mixinClassName.contains("LevelChunkMixin")
                || mixinClassName.contains("ModelDataManagerMixin")
                || mixinClassName.contains("SortedArraySetMixin")
                || mixinClassName.contains("RegistryOpsMemoizedMixin")
                || mixinClassName.contains("LevelStorageSourceMixin")
                || mixinClassName.contains("ShapeCacheRSMixin")
                || mixinClassName.contains("ShapeCacheCyclicMixin")
                || mixinClassName.contains("WorldSelectionListMixin")
                || mixinClassName.contains("chunk_deadlock")
                || mixinClassName.contains("world_leaks")
                || mixinClassName.contains("removed_dimensions")
                || mixinClassName.contains("missing_block_entities")
                || mixinClassName.contains("entity_pose_stack")
                || mixinClassName.contains("buffer_builder_leak")
                || mixinClassName.contains("extra_experimental_screen")
                || mixinClassName.contains("forge_vehicle_packets")
                || mixinClassName.contains("model_data_manager")
                || mixinClassName.contains("paper_chunk")
                || mixinClassName.contains("registry_ops")
                || mixinClassName.contains("unsafe_modded_shape")
                || mixinClassName.contains("world_screen_skipped")
                || mixinClassName.contains("ender_dragon_leak")
                || mixinClassName.contains("concurrency.");
    }

    private static boolean modLoading(String modId) {
        try {
            return LoadingModList.get().getModFileById(modId) != null;
        } catch (Throwable t) {
            // If the FML lookup shape ever changes, assume present: the mixin then fails loudly
            // at apply time in packs that lack the mod, instead of the compat fixes silently
            // vanishing in the packs that need them.
            return true;
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode classNode, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode classNode, String mixinClassName, IMixinInfo mixinInfo) {}
}
