package com.simibubi.create.modules.contraptions.components.contraptions;

import static com.simibubi.create.foundation.utility.AngleHelper.angleLerp;
import static com.simibubi.create.foundation.utility.AngleHelper.getShortestAngleDiff;

import com.simibubi.create.AllEntities;
import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.collision.CollisionManager;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.components.contraptions.bearing.BearingContraption;
import java.util.Objects;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.commons.lang3.tuple.MutablePair;

public class ContraptionEntity extends Entity implements IEntityAdditionalSpawnData {

  private static final DataParameter<Boolean> STALLED = EntityDataManager.createKey(
      ContraptionEntity.class,
      DataSerializers.BOOLEAN);
  private float prevYaw;
  private float prevPitch;
  private float prevRoll;
  private float yaw;
  public float pitch;
  private float roll;
  protected Contraption contraption;
  private float initialAngle;
  private BlockPos controllerPos;
  private IControlContraption controllerTE;
  private Vec3d motionBeforeStall;
  private boolean stationary;
  // Mounted Contraptions
  private float targetYaw;
  private float targetPitch;
  private CollisionManager collisionManager;

  public ContraptionEntity(EntityType<?> entityTypeIn, World worldIn) {
    super(entityTypeIn, worldIn);
    motionBeforeStall = Vec3d.ZERO;
    stationary = entityTypeIn == AllEntities.STATIONARY_CONTRAPTION.type;
    collisionManager = new CollisionManager(this);
  }

  public static ContraptionEntity createMounted(World world, Contraption contraption,
                                                float initialAngle) {
    ContraptionEntity entity = new ContraptionEntity(AllEntities.CONTRAPTION.type, world);
    entity.contraption = contraption;
    entity.initialAngle = initialAngle;
    entity.prevYaw = initialAngle;
    entity.yaw = initialAngle;
    entity.targetYaw = initialAngle;
    if (contraption != null) {
      contraption.gatherStoredItems();
    }
    return entity;
  }

  public static ContraptionEntity createStationary(World world, Contraption contraption) {
    ContraptionEntity entity = new ContraptionEntity(AllEntities.STATIONARY_CONTRAPTION.type,
                                                     world);
    entity.contraption = contraption;
    if (contraption != null) {
      contraption.gatherStoredItems();
    }
    return entity;
  }

    public <T extends TileEntity & IControlContraption> ContraptionEntity controlledBy(T controller) {
      controllerPos = controller.getPos();
      controllerTE = controller;
        return this;
    }

    @Override
    public void tick() {
        if (contraption == null) {
            remove();
            return;
        }

    attachToController();

		Entity e = getRidingEntity();
		if (e != null) {
			Entity riding = e;
			while (riding.getRidingEntity() != null)
				riding = riding.getRidingEntity();
			Vec3d movementVector = riding.getMotion();
			if (riding instanceof BoatEntity)
				movementVector = new Vec3d(posX - prevPosX, posY - prevPosY, posZ - prevPosZ);
			Vec3d motion = movementVector.normalize();
			if (motion.length() > 0) {
				targetYaw = ContraptionEntity.yawFromVector(motion);
				if (targetYaw < 0)
					targetYaw += 360;
				if (yaw < 0)
					yaw += 360;
			}

//			if (Math.abs(getShortestAngleDiff(yaw, targetYaw)) >= 175) {
//				initialAngle += 180;
//				yaw += 180;
//				prevYaw = yaw;
//			} else {
			float speed = 0.2f;
			prevYaw = yaw;
			yaw = angleLerp(speed, yaw, targetYaw);
//			}

			boolean wasStalled = isStalled();
			tickActors(movementVector);
			if (isStalled()) {
				if (!wasStalled)
					motionBeforeStall = riding.getMotion();
				riding.setMotion(0, 0, 0);
			}

			if (wasStalled && !isStalled()) {
				riding.setMotion(motionBeforeStall);
				motionBeforeStall = Vec3d.ZERO;
			}

      super.tick();
      return;
    }

		if (getMotion().length() > 1 / 4098f)
			translate(getMotion().x, getMotion().y, getMotion().z);
		tickActors(new Vec3d(posX - prevPosX, posY - prevPosY, posZ - prevPosZ));

    prevYaw = yaw;
    prevPitch = pitch;
    prevRoll = roll;

		super.tick();
	}

	private void tickActors(Vec3d movementVector) {
		float anglePitch = getPitch(1);
		float angleYaw = getYaw(1);
		float angleRoll = getRoll(1);
		Vec3d rotationVec = new Vec3d(angleRoll, angleYaw, anglePitch);
		Vec3d rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
		boolean stalledPreviously = contraption.stalled;

    if (!world.isRemote) {
      contraption.stalled = false;
    }

    for (MutablePair<Template.BlockInfo, MovementContext> pair : contraption.actors) {
      MovementContext context = pair.right;
      Template.BlockInfo blockInfo = pair.left;
      MovementBehaviour actor = Contraption.getMovement(blockInfo.state);

            Vec3d actorPosition = new Vec3d(blockInfo.pos);
      actorPosition = actorPosition.add(
          Objects.requireNonNull(actor).getActiveAreaOffset(context));
            actorPosition = VecHelper.rotate(actorPosition, angleRoll, angleYaw, anglePitch);
            actorPosition = actorPosition.add(rotationOffset).add(posX, posY, posZ);

            boolean newPosVisited = false;
            BlockPos gridPosition = new BlockPos(actorPosition);

			if (!stalledPreviously) {
				Vec3d previousPosition = context.position;
				if (previousPosition != null) {
					context.motion = actorPosition.subtract(previousPosition);
					Vec3d relativeMotion = context.motion;
					relativeMotion = VecHelper.rotate(relativeMotion, -angleRoll, -angleYaw, -anglePitch);
					context.relativeMotion = relativeMotion;
					newPosVisited = !new BlockPos(previousPosition).equals(gridPosition)
							|| context.relativeMotion.length() > 0 && context.firstMovement;
				}

				if (getContraption() instanceof BearingContraption) {
					BearingContraption bc = (BearingContraption) getContraption();
					Direction facing = bc.getFacing();
					if (VecHelper.onSameAxis(blockInfo.pos, BlockPos.ZERO, facing.getAxis())) {
						context.motion = new Vec3d(facing.getDirectionVec()).scale(
								facing.getAxis().getCoordinate(roll - prevRoll, yaw - prevYaw, pitch - prevPitch));
						context.relativeMotion = context.motion;
						int timer = context.data.getInt("StationaryTimer");
						if (timer > 0) {
							context.data.putInt("StationaryTimer", timer - 1);
						} else {
							context.data.putInt("StationaryTimer", 20);
							newPosVisited = true;
						}
					}
				}
			}

      context.rotation = rotationVec;
      context.position = actorPosition;

      if (actor.isActive(context)) {
        if (newPosVisited && !context.stall) {
          actor.visitNewPosition(context, gridPosition);
          context.firstMovement = false;
        }
        actor.tick(context);
        contraption.stalled |= context.stall;
      }
    }

    if (!world.isRemote) {
      if (!stalledPreviously && contraption.stalled) {
        setMotion(Vec3d.ZERO);
        if (controllerTE != null) {
          controllerTE.onStall();
        }
        AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> this),
                                new ContraptionStallPacket(getEntityId(), posX, posY, posZ, yaw,
                                                           pitch, roll));
      }
      dataManager.set(ContraptionEntity.STALLED, contraption.stalled);
    } else {
      contraption.stalled = isStalled();
    }
  }

    public static float yawFromVector(Vec3d vec) {
        return (float) ((3 * Math.PI / 2 + Math.atan2(vec.z, vec.x)) / Math.PI * 180);
    }

    private void translate(double x, double y, double z) {
      com.simibubi.create.foundation.collision.Vec3d translationProgress =
          collisionManager.tryTranslation(posX, this.posY, posZ, x, y, z);

      double destX = posX + translationProgress.getX();
      double destY = posY + translationProgress.getY();
      double destZ = posZ + translationProgress.getZ();
      setPosition(destX, destY, destZ);
    }

  public void rotateTo(double roll, double pitch, double yaw) {
    rotate(getShortestAngleDiff(this.roll, roll), getShortestAngleDiff(this.pitch, pitch),
           getShortestAngleDiff(this.yaw, yaw)
          );
  }

    public static EntityType.Builder<?> build(EntityType.Builder<?> builder) {
        @SuppressWarnings("unchecked")
        EntityType.Builder<ContraptionEntity> entityBuilder = (EntityType.Builder<ContraptionEntity>) builder;
        return entityBuilder.size(1, 1);
    }

    @OnlyIn(Dist.CLIENT)
    static void handleStallPacket(ContraptionStallPacket packet) {
        Entity entity = Minecraft.getInstance().world.getEntityByID(packet.entityID);
        if (!(entity instanceof ContraptionEntity))
            return;
        ContraptionEntity ce = (ContraptionEntity) entity;
        if (ce.getRidingEntity() == null) {
            ce.posX = packet.x;
            ce.posY = packet.y;
            ce.posZ = packet.z;
        }
        ce.yaw = packet.yaw;
        ce.pitch = packet.pitch;
        ce.roll = packet.roll;
    }

    private void translateTo(double x, double y, double z) {
        translate(x - posX, y - posY, z - posZ);
    }

	@Override
	public void setPosition(double x, double y, double z) {
		Entity e = getRidingEntity();
    if (e instanceof AbstractMinecartEntity) {
			Entity riding = e;
			while (riding.getRidingEntity() != null)
				riding = riding.getRidingEntity();
			x = riding.posX - .5;
			z = riding.posZ - .5;
		}

    posX = x;
    posY = y;
    posZ = z;

		if (isAddedToWorld() && !world.isRemote && world instanceof ServerWorld)
			((ServerWorld) world).chunkCheck(this); // Forge - Process chunk registration after moving.
		if (contraption != null) {
			AxisAlignedBB cbox = contraption.getBoundingBox();
			if (cbox != null)
        setBoundingBox(cbox.offset(x, y, z));
		}
	}

  private void rotate(double roll, double pitch, double yaw) {
    com.simibubi.create.foundation.collision.Vec3d rotationProgress =
        collisionManager.tryRotation(this.roll, this.pitch, this.yaw, roll, pitch, yaw);

    // x-axis rotation
    this.roll += rotationProgress.getX();
    // y-axis rotation
    this.pitch += rotationProgress.getY();
    // z-axis rotation
    this.yaw += rotationProgress.getZ();
  }

	public static float pitchFromVector(Vec3d vec) {
		return (float) ((Math.acos(vec.y)) / Math.PI * 180);
	}

	public float getYaw(float partialTicks) {
		return (getRidingEntity() == null ? 1 : -1)
				* (partialTicks == 1.0F ? yaw : angleLerp(partialTicks, prevYaw, yaw)) + initialAngle;
	}

	public float getPitch(float partialTicks) {
		return partialTicks == 1.0F ? pitch : angleLerp(partialTicks, prevPitch, pitch);
	}

    float getRoll(float partialTicks) {
        return partialTicks == 1.0F ? roll : angleLerp(partialTicks, prevRoll, roll);
    }

	@Override
	protected void registerData() {
    dataManager.register(ContraptionEntity.STALLED, false);
	}

	@Override
	protected void readAdditional(CompoundNBT compound) {
		contraption = Contraption.fromNBT(world, compound.getCompound("Contraption"));
		initialAngle = compound.getFloat("InitialAngle");
		targetYaw = yaw = prevYaw = initialAngle;
		dataManager.set(ContraptionEntity.STALLED, compound.getBoolean("Stalled"));
		ListNBT vecNBT = compound.getList("CachedMotion", 6);
		if (!vecNBT.isEmpty()) {
			motionBeforeStall = new Vec3d(vecNBT.getDouble(0), vecNBT.getDouble(1), vecNBT.getDouble(2));
			if (!motionBeforeStall.equals(Vec3d.ZERO))
				targetYaw = prevYaw = yaw += ContraptionEntity.yawFromVector(motionBeforeStall);
			setMotion(Vec3d.ZERO);
		}
		if (compound.contains("Controller"))
			controllerPos = NBTUtil.readBlockPos(compound.getCompound("Controller"));
	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
		compound.put("Contraption", getContraption().writeNBT());
		compound.putFloat("InitialAngle", initialAngle);
		if (!stationary)
			compound.put("CachedMotion",
					newDoubleNBTList(motionBeforeStall.x, motionBeforeStall.y, motionBeforeStall.z));
		compound.putBoolean("Stalled", isStalled());
		if (controllerPos != null)
			compound.put("Controller", NBTUtil.writeBlockPos(controllerPos));
	}

    @Override
    public void stopRiding() {
        super.stopRiding();
        if (!world.isRemote)
            disassemble();
    }

	public void disassemble() {
		if (getContraption() != null) {
			float yaw = getYaw(1);
			getContraption().disassemble(world, new BlockPos(getPositionVec().add(.5, .5, .5)),
					new Vec3d(getRoll(1), yaw, getPitch(1)));
		}
		remove();
	}

    public Contraption getContraption() {
        return contraption;
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        CompoundNBT compound = new CompoundNBT();
        writeAdditional(compound);
        buffer.writeCompoundTag(compound);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        readAdditional(Objects.requireNonNull(additionalData.readCompoundTag()));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch,
                                             int posRotationIncrements, boolean teleport) {
        // Stationary Anchors are responsible for keeping position and motion in sync
        // themselves.
        if (stationary)
            return;
        super.setPositionAndRotationDirect(x, y, z, yaw, pitch, posRotationIncrements, teleport);
    }

    @Override
    public void notifyDataManagerChange(@Nonnull DataParameter<?> key) {
        super.notifyDataManagerChange(key);
    }

    @Override
    @Nonnull
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void setMotion(@Nonnull Vec3d motionIn) {
        // Make sure nothing can move contraptions out of the way
    }

    private void attachToController() {
        if (controllerPos != null && (controllerTE == null || !controllerTE.isValid())) {
            if (!world.isBlockPresent(controllerPos))
                return;
            TileEntity te = world.getTileEntity(controllerPos);
            if (!(te instanceof IControlContraption)) {
                remove();
                return;
            }
            IControlContraption controllerTE = (IControlContraption) te;
            this.controllerTE = controllerTE;
            controllerTE.attach(this);

            if (world.isRemote)
                setPosition(posX, posY, posZ);
        }
    }

    public boolean isStalled() {
        return dataManager.get(ContraptionEntity.STALLED);
    }

    public void setContraptionMotion(Vec3d vec) {
        super.setMotion(vec);
    }

}
