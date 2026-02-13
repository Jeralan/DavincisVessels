package com.tridevmc.davincisvessels.common.entity;

import com.tridevmc.davincisvessels.DavincisVesselsMod;
import com.tridevmc.davincisvessels.client.control.VesselControllerClient;
import com.tridevmc.davincisvessels.client.gui.ContainerVessel;
import com.tridevmc.davincisvessels.client.gui.GuiVessel;
import com.tridevmc.davincisvessels.common.IElementProvider;
import com.tridevmc.davincisvessels.common.api.tileentity.ITileEngineModifier;
import com.tridevmc.davincisvessels.common.control.EnumVesselControlType;
import com.tridevmc.davincisvessels.common.control.VesselControllerCommon;
import com.tridevmc.davincisvessels.common.tileentity.TileHelm;
import com.tridevmc.movingworld.common.chunk.LocatedBlock;
import com.tridevmc.movingworld.common.chunk.MovingWorldAssemblyInteractor;
import com.tridevmc.movingworld.common.chunk.assembly.AssembleResult;
import com.tridevmc.movingworld.common.chunk.assembly.ChunkDisassembler;
import com.tridevmc.movingworld.common.entity.EntityMovingWorld;
import com.tridevmc.movingworld.common.entity.MovingWorldCapabilities;
import com.tridevmc.movingworld.common.entity.MovingWorldHandlerCommon;
import com.tridevmc.movingworld.common.util.MathHelperMod;
import com.tridevmc.movingworld.common.util.Vec3dMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Set;

import javax.annotation.Nonnull;

public class EntityVessel extends EntityMovingWorld implements IElementProvider<ContainerVessel> {

    public static final EntityDataAccessor<Float> ENGINE_POWER = SynchedEntityData.defineId(EntityVessel.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Boolean> CAN_MOVE = SynchedEntityData.defineId(EntityVessel.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> CAN_SUBMERGE = SynchedEntityData.defineId(EntityVessel.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Byte> IS_SUBMERGED = SynchedEntityData.defineId(EntityVessel.class, EntityDataSerializers.BYTE);

    public static final float BASE_FORWARD_SPEED = 0.005F, BASE_TURN_SPEED = 0.5F, BASE_LIFT_SPEED = 0.004F;
    public VesselCapabilities capabilities;
    private VesselControllerCommon controller;
    private MovingWorldHandlerCommon handler;
    private VesselAssemblyInteractor vesselAssemblyInteractor;
    private int driftCooldown = 0;
    private boolean submerge;

    public EntityVessel(Level world) {
        super((EntityType<? extends EntityMovingWorld>) DavincisVesselsMod.CONTENT.entityTypes.get(EntityVessel.class).get(), world);
        capabilities = new VesselCapabilities(this, true);
    }

    @Override
    public EntityType<?> getType() {
        return DavincisVesselsMod.CONTENT.entityTypes.get(EntityVessel.class).get();
    }

    @Override
    public void assembleResultEntity() {
        super.assembleResultEntity();
    }

    @Override
    public void baseTick() {
        super.baseTick();

        if (level() != null) {
            if (!level().isClientSide) {
                driftCooldown -= 1;
                boolean hasEngines = false;
                if (capabilities.getEngines() != null) {
                    if (capabilities.getEngines().isEmpty())
                        hasEngines = false;
                    else {
                        hasEngines = capabilities.getEnginePower() > 0;
                    }
                }
                if (DavincisVesselsMod.CONFIG.enginesMandatory)
                    entityData.set(CAN_MOVE, hasEngines);
                else
                    entityData.set(CAN_MOVE, true);
            }
            if (level().isClientSide) {
                if (entityData != null && !entityData.isEmpty() && entityData.isDirty()) {
                    submerge = entityData.get(IS_SUBMERGED) == Byte.valueOf((byte) 1);
                }
            }
        }
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(DavincisVesselsMod.CONTENT.blockHelm.get());
    }

    public boolean getSubmerge() {
        return !entityData.isEmpty() ? (entityData.get(IS_SUBMERGED) == (byte) 1) : false;
    }

    public void setSubmerge(boolean submerge) {
        this.submerge = submerge;
        if (level() != null && !level().isClientSide) {
            entityData.set(IS_SUBMERGED, submerge ? Byte.valueOf((byte) 1) : Byte.valueOf((byte) 0));
            if (getMobileChunk().marker != null && getMobileChunk().marker.tile instanceof TileHelm) {
                TileHelm helm = (TileHelm) getMobileChunk().marker.tile;

                helm.submerge = submerge;
            }
        }
    }

    @Override
    public boolean canCollideWith(@Nonnull Entity entity) {
        return entity instanceof EntityMovingWorld;
    }

    @Override
    public MovingWorldHandlerCommon getHandler() {
        if (handler == null) {
            if (level().isClientSide) {
                handler = new VesselHandlerClient(this);
                handler.setMovingWorld(this);
            } else {
                handler = new VesselHandlerServer(this);
                handler.setMovingWorld(this);
            }
        }
        return handler;
    }

    @Override
    public void initMovingWorld() {
        entityData.define(ENGINE_POWER, 0F);
        entityData.define(CAN_MOVE, false);
        entityData.define(CAN_SUBMERGE, false);
        entityData.define(IS_SUBMERGED, (byte) 0);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initMovingWorldClient() {
        handler = new VesselHandlerClient(this);
        controller = new VesselControllerClient();
    }

    @Override
    public void initMovingWorldCommon() {
        handler = new VesselHandlerServer(this);
        controller = new VesselControllerCommon();
    }

    @Override
    public MovingWorldCapabilities getMovingWorldCapabilities() {
        return this.capabilities == null ? new VesselCapabilities(this, true) : this.capabilities;
    }

    @Override
    public void setCapabilities(MovingWorldCapabilities capabilities) {
        if (capabilities instanceof VesselCapabilities) {
            this.capabilities = (VesselCapabilities) capabilities;
        }
    }

    /**
     * Aligns to the closest anchor within the radius specified in the configuration.
     */
    public boolean alignToAnchor() {
        ImmutablePair<LocatedBlock, LocatedBlock> closestRelation = capabilities.findClosestValidAnchor(DavincisVesselsMod.CONFIG.anchorRadius);
        if (!closestRelation.getLeft().equals(LocatedBlock.AIR)
                && !closestRelation.getRight().equals(LocatedBlock.AIR)) {
            BlockPos chunkAnchor = closestRelation.getLeft().pos;
            BlockPos worldAnchor = closestRelation.getRight().pos;
            super.alignToGrid(true);

            float yaw = Math.round(getYRot() / 90F) * 90F;
            yaw = (float) Math.toRadians(yaw);
            float ox = -getMobileChunk().getCenterX();
            float oz = -getMobileChunk().getCenterZ();
            Vec3dMod vecB = new Vec3dMod(chunkAnchor.getX() + ox, 0, chunkAnchor.getZ() + oz);
            Vec3dMod vec = vecB;
            vec = vec.rotateAroundY(yaw);

            BlockPos pos = new BlockPos(MathHelperMod.round_double(vec.x), 0, MathHelperMod.round_double(vec.z));
            moveTo(
                    worldAnchor.getX() + -pos.getX(), worldAnchor.getY() + 2, worldAnchor.getZ() + -pos.getZ());

            super.alignToGrid(false);
            positionRider(getControllingPassenger());

            return true;
        }
        return false;
    }

    @Override
    public void alignToGrid(boolean doPosAdjustment) {
        if (!alignToAnchor())
            super.alignToGrid(true);
    }

    @Override
    public boolean isBraking() {
        return controller.getVesselControl() == 3;
    }

    @Override
    public MovingWorldAssemblyInteractor getNewAssemblyInteractor() {
        return new VesselAssemblyInteractor();
    }

    @Override
    public void writeMovingWorldNBT(CompoundTag tag) {
        tag.putBoolean("submerge", submerge);
    }

    @Override
    public void readMovingWorldNBT(CompoundTag tag) {
        setSubmerge(tag.getBoolean("submerge"));
    }

    @Override
    public void writeMovingWorldSpawnData(ByteBuf data) {
    }

    @Override
    public void handleControl(double horizontalVelocity) {
        capabilities.updateEngines();

        if (getControllingPassenger() == null) {
            if (prevRiddenByEntity != null) {
                if (DavincisVesselsMod.CONFIG.disassembleOnDismount) {
                    alignToGrid(true);
                    updatePassengerPosition(prevRiddenByEntity, riderDestination, 1);
                    disassemble(false);
                } else {
                    if (!level().isClientSide && isFlying()) {
                        driftCooldown = 20 * 6;
                        EntityParachute parachute = new EntityParachute(level(), this, riderDestination);
                        if (((ServerLevel) level()).addFreshEntity(parachute)) {
                            prevRiddenByEntity.startRiding(parachute);
                            prevRiddenByEntity.setPose(Pose.STANDING);
                        }
                    }
                }
                prevRiddenByEntity = null;
            }
        }

        if (getControllingPassenger() == null || !capabilities.canMove()) {
            if (isFlying()) {
                this.setDeltaMovement(this.getDeltaMovement().subtract(0, BASE_LIFT_SPEED * 0.2F, 0));
            }
        } else {
            handlePlayerControl();
            prevRiddenByEntity = getControllingPassenger();
        }
    }

    @Override
    public void updatePassengerPosition(Entity passenger, BlockPos riderDestination, int flags) {
        super.updatePassengerPosition(passenger, riderDestination, flags);

        if (submerge && passenger instanceof LivingEntity && level() != null && !level().isClientSide) {
            //Apply water breathing so we don't die and apply night vision so we're not blind.

            LivingEntity livingPassenger = (LivingEntity) passenger;
            if (livingPassenger.getEffect(MobEffects.WATER_BREATHING) == null ||
                    livingPassenger.getEffect(MobEffects.WATER_BREATHING).getDuration() <= 20 * 11)
                livingPassenger.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 20 * 12, 1));
            if (livingPassenger.getEffect(MobEffects.NIGHT_VISION) == null ||
                    livingPassenger.getEffect(MobEffects.NIGHT_VISION).getDuration() <= 20 * 11)
                livingPassenger.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 12, 1));
        }
    }

    /**
     * Currently overridden for future combat system.
     */
    protected String getHurtSound() {
        return "mob.irongolem.hit";
    }

    /**
     * Currently overridden for future combat system.
     */
    protected String getDeathSound() {
        return "mob.irongolem.death";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void spawnParticles(double horvel) {
        if (capabilities.getEngines() != null && !capabilities.getEngines().isEmpty()) {
            Vec3dMod vec = Vec3dMod.getOrigin();
            float yaw = (float) Math.toRadians(getYRot());
            for (ITileEngineModifier engine : capabilities.getEngines()) {
                if (engine.getPowerIncrement(capabilities) != 0F) {
                    vec = vec.setX(((BlockEntity) engine).getBlockPos().getX() - getMobileChunk().getCenterX() + 0.5f);
                    vec = vec.setY(((BlockEntity) engine).getBlockPos().getY());
                    vec = vec.setZ(((BlockEntity) engine).getBlockPos().getZ() - getMobileChunk().getCenterZ() + 0.5f);
                    vec = vec.rotateAroundY(yaw);
                    level().addParticle(ParticleTypes.LARGE_SMOKE,
                            getX() + vec.x, getY() + vec.y + 1d, getZ() + vec.z, 0d, 0d, 0d);
                }
            }
        }
    }

    public int getBelowWater() {
        byte b0 = 5;
        int blocksPerMeter = (int) (b0 * (getBoundingBox().maxY - getBoundingBox().minY));
        AABB axisalignedbb = new AABB(0D, 0D, 0D, 0D, 0D, 0D);
        int belowWater = 0;
        for (; belowWater < blocksPerMeter; belowWater++) {
            double d1 = getBoundingBox().minY
                    + (getBoundingBox().maxY - getBoundingBox().minY) * belowWater / blocksPerMeter;
            double d2 = getBoundingBox().minY
                    + (getBoundingBox().maxY - getBoundingBox().minY) * (belowWater + 1) / blocksPerMeter;
            axisalignedbb = new AABB(getBoundingBox().minX, d1, getBoundingBox().minZ, getBoundingBox().maxX, d2, getBoundingBox().maxZ);

            if (!isAABBInLiquidNotFall(level(), axisalignedbb)) {
                break;
            }
        }

        return belowWater;
    }

    @Override
    public void handleServerUpdate(double horizontalVelocity) {
        boolean submergeMode = getSubmerge();

        byte b0 = 5;
        int blocksPerMeter = (int) (b0 * (getBoundingBox().maxY - getBoundingBox().minY));
        float waterVolume = 0F;
        AABB axisalignedbb = new AABB(0D, 0D, 0D, 0D, 0D, 0D);
        int belowWater = 0;
        for (; belowWater < blocksPerMeter; belowWater++) {
            double d1 = getBoundingBox().minY
                    + (getBoundingBox().maxY - getBoundingBox().minY) * belowWater / blocksPerMeter;
            double d2 = getBoundingBox().minY
                    + (getBoundingBox().maxY - getBoundingBox().minY) * (belowWater + 1) / blocksPerMeter;
            axisalignedbb = new AABB(getBoundingBox().minX, d1, getBoundingBox().minZ, getBoundingBox().maxX, d2, getBoundingBox().maxZ);

            if (!isAABBInLiquidNotFall(level(), axisalignedbb)) {
                break;
            }
        }
        if (belowWater > 0 && layeredBlockVolumeCount != null) {
            int k = belowWater / b0;
            for (int y = 0; y <= k && y < layeredBlockVolumeCount.length; y++) {
                if (y == k) {
                    waterVolume += layeredBlockVolumeCount[y] * (belowWater % b0) * 1F / b0;
                } else {
                    waterVolume += layeredBlockVolumeCount[y] * 1F;
                }
            }
        }

        if (onGround()) {
            setFlying(false);
        }

        float gravity = 0.05F;
        if (waterVolume > 0F && !submergeMode) {
            setFlying(false);
            float buoyancyforce = 1F * waterVolume * gravity; //F = rho * V * g (Archimedes' principle)
            float mass = getMovingWorldCapabilities().getMass();
            setDeltaMovement(getDeltaMovement().add(0, buoyancyforce / mass, 0));
        }

        if (DavincisVesselsMod.CONFIG.enableVesselDownfall) {
            if (!isFlying() || (submergeMode && belowWater <= (getMobileChunk().maxY() * 5 / 3 * 2)))
                setDeltaMovement(getDeltaMovement().subtract(0, gravity, 0));
        } else {
            if (!capabilities.canFly() && !capabilities.canSubmerge())
                setDeltaMovement(getDeltaMovement().subtract(0, gravity, 0));
        }

        super.handleServerUpdate(horizontalVelocity);
    }

    @Override
    public void handleServerUpdatePreRotation() {
        if (DavincisVesselsMod.CONFIG.vesselControlType == EnumVesselControlType.VANILLA) {
            double newYaw = getYRot();
            double dx = xOld - getX();
            double dz = zOld - getZ();

            if (getControllingPassenger() != null && !isBraking() && dx * dx + dz * dz > 0.01D) {
                newYaw = 270F - Math.toDegrees(Math.atan2(dz, dx)) + frontDirection.get2DDataValue() * 90F;
            }

            double deltayaw = Mth.wrapDegrees(newYaw - getYRot());
            double maxyawspeed = 2D;
            if (deltayaw > maxyawspeed) {
                deltayaw = maxyawspeed;
            }
            if (deltayaw < -maxyawspeed) {
                deltayaw = -maxyawspeed;
            }

            setYRot((float) (getYRot() + deltayaw));
        }
    }

    @Override
    public boolean disassemble(boolean overwrite) {
        if (level().isClientSide)
            return true;

        positionRider(getControllingPassenger());

        ChunkDisassembler disassembler = getDisassembler();
        disassembler.overwrite = overwrite;

        if (!disassembler.canDisassemble(getNewAssemblyInteractor())) {
            if (prevRiddenByEntity instanceof Player) {
                MutableComponent testMessage = Component.literal("Cannot disassemble vessel here");
                ((Player) prevRiddenByEntity).displayClientMessage(testMessage, true);
            }
            return false;
        }

        AssembleResult result = disassembler.doDisassemble(getNewAssemblyInteractor());
        if (result.getMovingWorldMarker() != null) {
            BlockEntity te = result.getMovingWorldMarker().tile;
            if (te instanceof TileHelm) {
                ((TileHelm) te).setAssembleResult(result);
                ((TileHelm) te).setInfo(getInfo());
            }
        }

        return true;
    }

    private void handlePlayerControl() {
        if (getControllingPassenger() instanceof LivingEntity && ((VesselCapabilities) getMovingWorldCapabilities()).canMove()) {
            double throttle = ((LivingEntity) getControllingPassenger()).zza;
            if (isFlying()) {
                throttle *= 0.5D;
            }

            if (DavincisVesselsMod.CONFIG.vesselControlType == EnumVesselControlType.DAVINCIS) {
                Vec3dMod vec = new Vec3dMod(getControllingPassenger().getDeltaMovement().x, 0D, getControllingPassenger().getDeltaMovement().z);
                vec.rotateAroundY((float) Math.toRadians(getControllingPassenger().getYRot()));

                double steer = ((LivingEntity) getControllingPassenger()).xxa;
                motionYaw += steer * BASE_TURN_SPEED * capabilities.getRotationMult()
                        * DavincisVesselsMod.CONFIG.turnSpeed;

                float yaw = (float) Math.toRadians(180F - getYRot() + frontDirection.get2DDataValue() * 90F);
                vec = vec.setX(getDeltaMovement().x);
                vec = vec.setZ(getDeltaMovement().z);
                vec = vec.rotateAroundY(yaw);
                vec = vec.setX(vec.x * 0.9D);
                vec = vec.setZ(vec.z - throttle * BASE_FORWARD_SPEED * capabilities.getSpeedMult());
                vec = vec.rotateAroundY(-yaw);
                vec = vec.setY(getDeltaMovement().y);
                this.setDeltaMovement(vec);
            } else if (DavincisVesselsMod.CONFIG.vesselControlType == EnumVesselControlType.VANILLA) {
                if (throttle > 0.0D) {
                    double dsin = -Math.sin(Math.toRadians(getControllingPassenger().getYRot()));
                    double dcos = Math.cos(Math.toRadians(getControllingPassenger().getYRot()));

                    this.setDeltaMovement(this.getDeltaMovement().add(dsin * BASE_FORWARD_SPEED * capabilities.speedMultiplier, 0,
                            dcos * BASE_FORWARD_SPEED * capabilities.speedMultiplier));
                }
            }
        }

        if (controller.getVesselControl() != 0) {
            if (controller.getVesselControl() == 4) {
                alignToGrid(true);
            } else if (isBraking()) {
                float yMult = isFlying() ? capabilities.brakeMult : 1;
                this.setDeltaMovement(this.getDeltaMovement().multiply(capabilities.brakeMult, yMult, capabilities.brakeMult));
            } else if (controller.getVesselControl() < 3 && capabilities.canFly()) {
                int i;
                if (controller.getVesselControl() == 2) {
                    setFlying(true);
                    i = 1;
                } else {
                    i = -1;
                }
                this.setDeltaMovement(this.getDeltaMovement().add(0, i * BASE_LIFT_SPEED * capabilities.getLiftMult(), 0));
                // TODO: Achievements are gone.
                //if (getControllingPassenger() != null && getControllingPassenger() instanceof EntityPlayer
                //        && !((EntityPlayer) getControllingPassenger()).hasAchievement(DavincisVesselsContent.achievementFlyVessel))
                //    ((EntityPlayer) getControllingPassenger()).addStat(DavincisVesselsContent.achievementFlyVessel);
            }
        }
    }

    @Override
    public boolean isPushable() {
        return !isRemoved() && getControllingPassenger() == null && driftCooldown <= 0;
    }

    @Override
    public boolean isFlying() {
        return (capabilities.canFly() && (super.isFlying() || controller.getVesselControl() == 2)) || getSubmerge();
    }

    public boolean areSubmerged() {
        int belowWater = getBelowWater();

        return getSubmerge() && belowWater > 0;
    }

    @Override
    public void readMovingWorldSpawnData(ByteBuf data) {
    }

    @Override
    public float getXRenderScale() {
        return 1F;
    }

    @Override
    public float getYRenderScale() {
        return 1F;
    }

    @Override
    public float getZRenderScale() {
        return 1F;
    }

    @Override
    public MovingWorldAssemblyInteractor getAssemblyInteractor() {
        if (vesselAssemblyInteractor == null)
            vesselAssemblyInteractor = (VesselAssemblyInteractor) getNewAssemblyInteractor();

        return vesselAssemblyInteractor;
    }

    @Override
    public void setAssemblyInteractor(MovingWorldAssemblyInteractor interactor) {
        //vesselAssemblyInteractor = (VesselAssemblyInteractor) interactor;
        //interactor.transferToCapabilities(getMovingWorldCapabilities());
    }

    @Override
    public void fillAirBlocks(Set<BlockPos> set, BlockPos pos) {
        super.fillAirBlocks(set, pos);
    }

    public VesselControllerCommon getController() {
        return controller;
    }

    public boolean canSubmerge() {
        return !entityData.isEmpty() ? entityData.get(CAN_SUBMERGE) : false;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public AbstractContainerScreen<ContainerVessel> createScreen(ContainerVessel container, Player player) {
        return new GuiVessel(container);
    }

    @Override
    public AbstractContainerMenu createMenu(int window, @Nonnull Inventory playerInventory, @Nonnull Player playerIn) {
        return new ContainerVessel(window, this, playerIn);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}