package com.simibubi.create.modules.contraptions.components.contraptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.WrappedWorld;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.components.contraptions.chassis.AbstractChassisBlock;
import com.simibubi.create.modules.contraptions.components.contraptions.chassis.ChassisTileEntity;
import com.simibubi.create.modules.contraptions.components.saw.SawBlock;
import com.simibubi.create.modules.contraptions.redstone.ContactBlock;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.modules.logistics.block.inventories.FlexcrateBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.SlimeBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;

public abstract class Contraption {

	public Map<BlockPos, BlockInfo> blocks;
	public Map<BlockPos, MountedStorage> storage;
	public List<MutablePair<BlockInfo, MovementContext>> actors;
	public CombinedInvWrapper inventory;
	public List<TileEntity> customRenderTEs;

	public AxisAlignedBB bounds;
	public boolean stalled;

	protected Set<BlockPos> cachedColliders;
	protected Direction cachedColliderDirection;
	protected BlockPos anchor;

	List<BlockPos> renderOrder;

	public Contraption() {
		blocks = new HashMap<>();
		storage = new HashMap<>();
		actors = new ArrayList<>();
		renderOrder = new ArrayList<>();
		customRenderTEs = new ArrayList<>();
	}

	public Set<BlockPos> getColliders(World world, Direction movementDirection) {
		if (blocks == null)
			return null;
		if (cachedColliders == null || cachedColliderDirection != movementDirection) {
			cachedColliders = new HashSet<>();
			cachedColliderDirection = movementDirection;

			for (BlockInfo info : blocks.values()) {
				BlockPos offsetPos = info.pos.offset(movementDirection);
				boolean hasNext = false;
				for (BlockInfo otherInfo : blocks.values()) {
					if (!otherInfo.pos.equals(offsetPos))
						continue;
					hasNext = true;
					break;
				}
				if (!hasNext)
					cachedColliders.add(info.pos);
			}

		}
		return cachedColliders;
	}

	public boolean searchMovedStructure(World world, BlockPos pos, @Nullable Direction forcedDirection) {
		List<BlockPos> frontier = new ArrayList<>();
		Set<BlockPos> visited = new HashSet<>();
		anchor = pos;

		if (bounds == null)
			bounds = new AxisAlignedBB(BlockPos.ZERO);

		frontier.add(pos);
		if (!addToInitialFrontier(world, pos, forcedDirection, frontier))
			return false;

		for (int limit = 1000; limit > 0; limit--) {
			if (frontier.isEmpty())
				return true;
			if (!moveBlock(world, frontier.remove(0), forcedDirection, frontier, visited))
				return false;
		}
		return false;
	}

	public void gatherStoredItems() {
		List<IItemHandlerModifiable> list =
			storage.values().stream().map(MountedStorage::getItemHandler).collect(Collectors.toList());
		inventory = new CombinedInvWrapper(Arrays.copyOf(list.toArray(), list.size(), IItemHandlerModifiable[].class));
	}

	protected boolean addToInitialFrontier(World world, BlockPos pos, Direction forcedDirection,
			List<BlockPos> frontier) {
		return true;
	}

	protected boolean moveBlock(World world, BlockPos pos, Direction forcedDirection, List<BlockPos> frontier,
			Set<BlockPos> visited) {
		visited.add(pos);
		frontier.remove(pos);

		if (!world.isBlockPresent(pos))
			return false;
		BlockState state = world.getBlockState(pos);
		if (state.getMaterial().isReplaceable())
			return true;
		if (state.getCollisionShape(world, pos).isEmpty())
			return true;
		if (!BlockMovementTraits.movementAllowed(world, pos))
			return false;
		if (isChassis(state) && !moveChassis(world, pos, forcedDirection, frontier, visited))
			return false;
		if (AllBlocks.FLEXCRATE.typeOf(state))
			FlexcrateBlock.splitCrate(world, pos);
		if (AllBlocks.BELT.typeOf(state)) {
			BlockPos nextPos = BeltBlock.nextSegmentPosition(state, pos, true);
			BlockPos prevPos = BeltBlock.nextSegmentPosition(state, pos, false);
			if (nextPos != null && !visited.contains(nextPos))
				frontier.add(nextPos);
			if (prevPos != null && !visited.contains(prevPos))
				frontier.add(prevPos);
		}

		if (state.getBlock() instanceof SlimeBlock)
			for (Direction offset : Direction.values()) {
				BlockPos offsetPos = pos.offset(offset);
				BlockState blockState = world.getBlockState(offsetPos);
				if (BlockMovementTraits.movementIgnored(blockState))
					continue;
				if (!BlockMovementTraits.movementAllowed(world, offsetPos)) {
					if (offset == forcedDirection)
						return false;
					continue;
				}
				if (!visited.contains(offsetPos))
					frontier.add(offsetPos);
			}

		add(pos, capture(world, pos));
		return true;
	}

	protected static boolean isChassis(BlockState state) {
		return state.getBlock() instanceof AbstractChassisBlock;
	}

	private boolean moveChassis(World world, BlockPos pos, Direction movementDirection, List<BlockPos> frontier,
			Set<BlockPos> visited) {
		TileEntity te = world.getTileEntity(pos);
		if (!(te instanceof ChassisTileEntity))
			return false;
		ChassisTileEntity chassis = (ChassisTileEntity) te;
		chassis.addAttachedChasses(frontier, visited);
		for (BlockPos blockPos : chassis.getIncludedBlockPositions(movementDirection, false))
			if (!visited.contains(blockPos))
				frontier.add(blockPos);
		return true;
	}

	public static Contraption fromNBT(World world, CompoundNBT nbt) {
		String type = nbt.getString("Type");
		Contraption contraption = AllContraptionTypes.fromType(type);
    Objects.requireNonNull(contraption).readNBT(world, nbt);
		return contraption;
	}

	public void readNBT(World world, CompoundNBT nbt) {
		blocks.clear();
		renderOrder.clear();
		customRenderTEs.clear();

		nbt.getList("Blocks", 10).forEach(c -> {
			CompoundNBT comp = (CompoundNBT) c;
			BlockInfo info = new BlockInfo(NBTUtil.readBlockPos(comp.getCompound("Pos")),
					NBTUtil.readBlockState(comp.getCompound("Block")),
					comp.contains("Data") ? comp.getCompound("Data") : null);
			blocks.put(info.pos, info);

			if (world.isRemote) {
				Block block = info.state.getBlock();
				BlockRenderLayer renderLayer = block.getRenderLayer();
				if (renderLayer == BlockRenderLayer.TRANSLUCENT)
					renderOrder.add(info.pos);
				else
					renderOrder.add(0, info.pos);
				CompoundNBT tag = info.nbt;
				if (tag == null || block instanceof IPortableBlock)
					return;

				tag.putInt("x", info.pos.getX());
				tag.putInt("y", info.pos.getY());
				tag.putInt("z", info.pos.getZ());

				TileEntity te = TileEntity.create(tag);
				Objects.requireNonNull(te).setWorld(new WrappedWorld(world) {

				  @Nonnull
					@Override
					public BlockState getBlockState(BlockPos pos) {
						if (!pos.equals(te.getPos()))
							return Blocks.AIR.getDefaultState();
						return info.state;
					}

				});
				if (te instanceof KineticTileEntity)
					((KineticTileEntity) te).setSpeed(0);
				te.getBlockState();
				customRenderTEs.add(te);
			}
		});

		actors.clear();
		nbt.getList("Actors", 10).forEach(c -> {
			CompoundNBT comp = (CompoundNBT) c;
			BlockInfo info = blocks.get(NBTUtil.readBlockPos(comp.getCompound("Pos")));
			MovementContext context = MovementContext.readNBT(world, info, comp);
			context.contraption = this;
			getActors().add(MutablePair.of(info, context));
		});

		storage.clear();
		nbt.getList("Storage", 10).forEach(c -> {
			CompoundNBT comp = (CompoundNBT) c;
			storage.put(NBTUtil.readBlockPos(comp.getCompound("Pos")), new MountedStorage(comp.getCompound("Data")));
		});
		List<IItemHandlerModifiable> list =
			storage.values().stream().map(MountedStorage::getItemHandler).collect(Collectors.toList());
		inventory = new CombinedInvWrapper(Arrays.copyOf(list.toArray(), list.size(), IItemHandlerModifiable[].class));

		if (nbt.contains("BoundsFront"))
			bounds = NBTHelper.readAABB(nbt.getList("BoundsFront", 5));

		stalled = nbt.getBoolean("Stalled");
		anchor = NBTUtil.readBlockPos(nbt.getCompound("Anchor"));
	}

  protected Pair<BlockInfo, TileEntity> capture(World world, BlockPos pos) {
    BlockState blockState = world.getBlockState(pos);
    if (AllBlocks.SAW.typeOf(blockState)) {
      blockState = blockState.with(SawBlock.RUNNING, true);
    }
    if (blockState.getBlock() instanceof ChestBlock) {
      blockState = blockState.with(ChestBlock.TYPE, ChestType.SINGLE);
    }
    if (AllBlocks.FLEXCRATE.typeOf(blockState)) {
      blockState = blockState.with(FlexcrateBlock.DOUBLE, false);
    }
    if (AllBlocks.CONTACT.typeOf(blockState)) {
      blockState = blockState.with(ContactBlock.POWERED, true);
    }
    CompoundNBT compoundNBT = getTileEntityNBT(world, pos);
    TileEntity tileEntity = world.getTileEntity(pos);
    return Pair.of(new BlockInfo(pos, blockState, compoundNBT), tileEntity);
  }

  public static CompoundNBT getTileEntityNBT(World world, BlockPos pos) {
    TileEntity tileEntity = world.getTileEntity(pos);
    CompoundNBT compoundNBT = null;
    if (tileEntity != null) {
      compoundNBT = tileEntity.write(new CompoundNBT());
      compoundNBT.remove("x");
      compoundNBT.remove("y");
      compoundNBT.remove("z");
    }
    return compoundNBT;
  }

  public void add(BlockPos pos, Pair<BlockInfo, TileEntity> pair) {
    BlockInfo captured = pair.getKey();
    BlockPos localPos = pos.subtract(anchor);
    BlockInfo blockInfo = new BlockInfo(localPos, captured.state, captured.nbt);

    if (blocks.put(localPos, blockInfo) != null) {
      return;
    }
    bounds = bounds.union(new AxisAlignedBB(localPos));

    TileEntity te = pair.getValue();
    if (MountedStorage.canUseAsStorage(te)) {
      storage.put(localPos, new MountedStorage(te));
    }
    if (captured.state.getBlock() instanceof IPortableBlock) {
      getActors().add(MutablePair.of(blockInfo, null));
    }
  }

	public CompoundNBT writeNBT() {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putString("Type", getType().id);
		ListNBT blocksNBT = new ListNBT();
		for (BlockInfo block : this.blocks.values()) {
			CompoundNBT c = new CompoundNBT();
			c.put("Block", NBTUtil.writeBlockState(block.state));
			c.put("Pos", NBTUtil.writeBlockPos(block.pos));
			if (block.nbt != null)
				c.put("Data", block.nbt);
			blocksNBT.add(c);
		}

		ListNBT actorsNBT = new ListNBT();
		for (MutablePair<BlockInfo, MovementContext> actor : getActors()) {
			CompoundNBT compound = new CompoundNBT();
			compound.put("Pos", NBTUtil.writeBlockPos(actor.left.pos));
      Objects.requireNonNull(getMovement(actor.left.state)).writeExtraData(actor.right);
			actor.right.writeToNBT(compound);
			actorsNBT.add(compound);
		}

		ListNBT storageNBT = new ListNBT();
		for (BlockPos pos : storage.keySet()) {
			CompoundNBT c = new CompoundNBT();
			MountedStorage mountedStorage = storage.get(pos);
			if (!mountedStorage.isWorking())
				continue;
			c.put("Pos", NBTUtil.writeBlockPos(pos));
			c.put("Data", mountedStorage.serialize());
			storageNBT.add(c);
		}

		nbt.put("Blocks", blocksNBT);
		nbt.put("Actors", actorsNBT);
		nbt.put("Storage", storageNBT);
		nbt.put("Anchor", NBTUtil.writeBlockPos(anchor));
		nbt.putBoolean("Stalled", stalled);

		if (bounds != null) {
			ListNBT bb = NBTHelper.writeAABB(bounds);
			nbt.put("BoundsFront", bb);
		}

		return nbt;
	}

	public static boolean isFrozen() {
		return AllConfigs.SERVER.control.freezePistonConstructs.get();
	}

	public void disassemble(World world, BlockPos offset, Vec3d rotation) {
		disassemble(world, offset, rotation, (pos, state) -> false);
	}

	public void removeBlocksFromWorld(IWorld world, BlockPos offset) {
		removeBlocksFromWorld(world, offset, (pos, state) -> false);
	}

  protected void removeBlocksFromWorld(IWorld world, BlockPos offset,
                                       BiPredicate<BlockPos, BlockState> customRemoval) {
		storage.values().forEach(MountedStorage::empty);
		for (BlockInfo block : blocks.values()) {
			BlockPos add = block.pos.add(anchor).add(offset);
			if (customRemoval.test(add, block.state))
				continue;
			world.getWorld().removeTileEntity(add);
			world.setBlockState(add, Blocks.AIR.getDefaultState(), 67);
		}
	}

  protected void disassemble(World world, BlockPos offset, Vec3d rotation,
                             BiPredicate<BlockPos, BlockState> customPlacement) {
		stop(world);

		StructureTransform transform = new StructureTransform(offset, rotation);

		for (BlockInfo block : blocks.values()) {
			BlockPos targetPos = transform.apply(block.pos);
			BlockState state = transform.apply(block.state);

			if (customPlacement.test(targetPos, state))
				continue;

			for (Direction face : Direction.values())
				state = state.updatePostPlacement(face, world.getBlockState(targetPos.offset(face)), world, targetPos,
						targetPos.offset(face));
			if (AllBlocks.SAW.typeOf(state))
				state = state.with(SawBlock.RUNNING, false);

			world.destroyBlock(targetPos, world.getBlockState(targetPos).getCollisionShape(world, targetPos).isEmpty());
			world.setBlockState(targetPos, state, 3 | BlockFlags.IS_MOVING);
			TileEntity tileEntity = world.getTileEntity(targetPos);
			CompoundNBT tag = block.nbt;
			if (tileEntity != null && tag != null) {
				tag.putInt("x", targetPos.getX());
				tag.putInt("y", targetPos.getY());
				tag.putInt("z", targetPos.getZ());

				if (tileEntity instanceof BeltTileEntity) {
					tag.remove("Length");
					tag.remove("Index");
					tag.putBoolean("DontClearAttachments", true);
				}

				tileEntity.read(tag);

				if (tileEntity instanceof KineticTileEntity) {
					KineticTileEntity kineticTileEntity = (KineticTileEntity) tileEntity;
					kineticTileEntity.source = null;
					kineticTileEntity.setSpeed(0);
					kineticTileEntity.network = null;
					kineticTileEntity.attachKinetics();
				}

				if (storage.containsKey(block.pos)) {
					MountedStorage mountedStorage = storage.get(block.pos);
					if (mountedStorage.isWorking())
						mountedStorage.fill(tileEntity);
				}
			}

		}
	}

	public void initActors(World world) {
		for (MutablePair<BlockInfo, MovementContext> pair : actors) {
			MovementContext context = new MovementContext(world, pair.left);
			context.contraption = this;
      Objects.requireNonNull(getMovement(pair.left.state)).startMoving(context);
			pair.setRight(context);
		}
	}

	public AxisAlignedBB getBoundingBox() {
		return bounds;
	}

	public List<MutablePair<BlockInfo, MovementContext>> getActors() {
		return actors;
	}

	public BlockPos getAnchor() {
		return anchor;
	}

	public void stop(World world) {
		foreachActor(world, (behaviour, ctx) -> {
			behaviour.stopMoving(ctx);
			ctx.position = null;
			ctx.motion = Vec3d.ZERO;
			ctx.relativeMotion = Vec3d.ZERO;
			ctx.rotation = Vec3d.ZERO;
		});
	}

	public void foreachActor(World world, BiConsumer<MovementBehaviour, MovementContext> callBack) {
		for (MutablePair<BlockInfo, MovementContext> pair : actors)
			callBack.accept(getMovement(pair.getLeft().state), pair.getRight());
	}

	protected static MovementBehaviour getMovement(BlockState state) {
		Block block = state.getBlock();
		if (!(block instanceof IPortableBlock))
			return null;
		return ((IPortableBlock) block).getMovementBehaviour();
	}

	public void expandBoundsAroundAxis(Axis axis) {
		AxisAlignedBB bb = bounds;
		double maxXDiff = Math.max(bb.maxX - 1, -bb.minX);
		double maxYDiff = Math.max(bb.maxY - 1, -bb.minY);
		double maxZDiff = Math.max(bb.maxZ - 1, -bb.minZ);
		double maxDiff = 0;

		if (axis == Axis.X)
			maxDiff = Math.max(maxZDiff, maxYDiff);
		if (axis == Axis.Y)
			maxDiff = Math.max(maxZDiff, maxXDiff);
		if (axis == Axis.Z)
			maxDiff = Math.max(maxXDiff, maxYDiff);

		Vec3d vec = new Vec3d(Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis).getDirectionVec());
		Vec3d planeByNormal = VecHelper.planeByNormal(vec);
		Vec3d min = vec.mul(bb.minX, bb.minY, bb.minZ).add(planeByNormal.scale(-maxDiff));
		Vec3d max = vec.mul(bb.maxX, bb.maxY, bb.maxZ).add(planeByNormal.scale(maxDiff + 1));
		bounds = new AxisAlignedBB(min, max);
	}

	protected abstract AllContraptionTypes getType();

}