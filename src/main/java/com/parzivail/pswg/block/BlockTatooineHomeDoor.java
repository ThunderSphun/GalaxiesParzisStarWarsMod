package com.parzivail.pswg.block;

import com.parzivail.pswg.blockentity.TatooineHomeDoorBlockEntity;
import com.parzivail.pswg.container.SwgBlocks;
import com.parzivail.util.block.RotatingBlock;
import com.parzivail.util.block.VoxelShapeUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class BlockTatooineHomeDoor extends RotatingBlock
{
	public static final EnumProperty<BlockHalf> HALF;
	private static final VoxelShape INTERACTION_SHAPE_CLOSED = VoxelShapes.union(
			VoxelShapes.cuboid(0, 1 - 0.0625, 0.25, 1, 1, 0.75),
			VoxelShapes.cuboid(0, 0, 0.25, 0.0625, 1, 0.75),
			VoxelShapes.cuboid(1 - 0.0625, 0, 0.25, 1, 1, 0.75),
			VoxelShapes.cuboid(0.0625, 0, 0.375, 1 - 0.0625, 1 - 0.0625, 0.625));
	private static final VoxelShape INTERACTION_SHAPE_OPEN = VoxelShapes.union(
			VoxelShapes.cuboid(0, 1 - 0.0625, 0.25, 1, 1, 0.75),
			VoxelShapes.cuboid(0, 0, 0.25, 0.0625, 1, 0.75),
			VoxelShapes.cuboid(1 - 0.0625, 0, 0.25, 1, 1, 0.75),
			VoxelShapes.cuboid(1 - 1.5 * 0.0625, 0, 0.375, 1 - 0.0625, 1 - 0.0625, 0.625));
	private static final VoxelShape COLLISION_SHAPE_CLOSED = VoxelShapes.union(
			VoxelShapes.cuboid(0, 0, 0.25, 0.0625, 1, 0.75),
			VoxelShapes.cuboid(1 - 0.0625, 0, 0.25, 1, 1, 0.75),
			VoxelShapes.cuboid(0.0625, 0, 0.375, 1 - 0.0625, 1 - 0.0625, 0.625));
	private static final VoxelShape COLLISION_SHAPE_OPEN = VoxelShapes.union(
			VoxelShapes.cuboid(0, 0, 0.25, 0.0625, 1, 0.75),
			VoxelShapes.cuboid(1 - 0.0625, 0, 0.25, 1, 1, 0.75),
			VoxelShapes.cuboid(1 - 1.5 * 0.0625, 0, 0.375, 1 - 0.0625, 1 - 0.0625, 0.625));
	protected static final EnumMap<Direction, VoxelShape> INTERACTION_SHAPES_CLOSED = new EnumMap<>(Direction.class);
	protected static final EnumMap<Direction, VoxelShape> INTERACTION_SHAPES_OPEN = new EnumMap<>(Direction.class);
	protected static final EnumMap<Direction, VoxelShape> COLLISION_SHAPES_CLOSED = new EnumMap<>(Direction.class);
	protected static final EnumMap<Direction, VoxelShape> COLLISION_SHAPES_OPEN = new EnumMap<>(Direction.class);

	static
	{
		HALF = Properties.BLOCK_HALF;

		Direction[] facingArray = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
		for (int i = 0; i < facingArray.length; i++)
		{
			INTERACTION_SHAPES_CLOSED.put(facingArray[i] , VoxelShapeUtil.rotate(INTERACTION_SHAPE_CLOSED, i));
			INTERACTION_SHAPES_OPEN.put(facingArray[i] , VoxelShapeUtil.rotate(INTERACTION_SHAPE_OPEN, i));
			COLLISION_SHAPES_CLOSED.put(facingArray[i] , VoxelShapeUtil.rotate(COLLISION_SHAPE_CLOSED, i));
			COLLISION_SHAPES_OPEN.put(facingArray[i] , VoxelShapeUtil.rotate(COLLISION_SHAPE_OPEN, i));
		}
	}

	public BlockTatooineHomeDoor(Settings settings)
	{
		super(settings);

		this.setDefaultState(this.stateManager.getDefaultState().with(HALF, BlockHalf.BOTTOM));
	}

	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		super.appendProperties(builder);
		builder.add(HALF);
	}

	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return this.getShape(state, world, pos, INTERACTION_SHAPES_OPEN, INTERACTION_SHAPES_CLOSED);
	}

	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return this.getShape(state, world, pos, COLLISION_SHAPES_OPEN, COLLISION_SHAPES_CLOSED);
	}

	protected VoxelShape getShape(BlockState state, BlockView world, BlockPos pos, Map<Direction, VoxelShape> openShapes, Map<Direction, VoxelShape> closedShapes)
	{
		BlockPos controllerPos = getController(world, pos);
		TatooineHomeDoorBlockEntity e = (TatooineHomeDoorBlockEntity)world.getBlockEntity(controllerPos);

		Direction facing = state.get(RotatingBlock.FACING);

		if (e == null || !e.isOpening() || e.isMoving())
			return openShapes.get(facing);

		return closedShapes.get(facing);
	}

	@Override
	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state)
	{
		return new ItemStack(SwgBlocks.Door.TatooineHomeController);
	}

	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		return this.getDefaultState().with(FACING, ctx.getPlayerFacing());
	}

	protected BlockPos getController(BlockView world, BlockPos self)
	{
		return self.down();
	}

	@SuppressWarnings("deprecation")
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
	{
		if (world.isClient)
			return ActionResult.SUCCESS;
		else
		{
			BlockPos controllerPos = getController(world, pos);
			TatooineHomeDoorBlockEntity e = (TatooineHomeDoorBlockEntity)world.getBlockEntity(controllerPos);

			assert e != null;

			if (!e.isMoving())
			{
				e.setPowered(false);
				e.startMoving();
				return ActionResult.SUCCESS;
			}

			return ActionResult.CONSUME;
		}
	}

	@SuppressWarnings("deprecation")
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify)
	{
		BlockPos controllerPos = getController(world, pos);
		TatooineHomeDoorBlockEntity e = (TatooineHomeDoorBlockEntity)world.getBlockEntity(controllerPos);

		if (e == null)
			return;

		boolean wasPowered = e.isPowered();
		boolean isPowered = world.isReceivingRedstonePower(pos);

		if (!world.isClient && block != this)
		{
			e.setPowered(isPowered);
			if (!e.isMoving())
			{
				if (wasPowered && !isPowered && !e.isOpening())
				{
					e.setDirection(false);
					e.startMoving();
				}
				else if (isPowered && !wasPowered && e.isOpening())
				{
					e.setDirection(true);
					e.startMoving();
				}
			}
		}
	}

	public boolean canPlace(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer)
	{
		for (int i = 1; i < HALF.stream().count(); i++)
		{
			pos = pos.up();
			if (!world.isAir(pos))
				return false;
		}

		return true;
	}

	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
	{
		super.onPlaced(world, pos, state, placer, itemStack);
		if (!world.isClient)
		{
			world.setBlockState(pos.up(), SwgBlocks.Door.TatooineHomeFiller.getDefaultState().with(FACING, state.get(FACING)).with(HALF, BlockHalf.TOP), 3);

			world.updateNeighbors(pos, Blocks.AIR);
			state.updateNeighbors(world, pos, 3);
		}
	}

	public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player)
	{
		if (!world.isClient)
		{
			if (state.get(HALF) == BlockHalf.BOTTOM) {
				destroyPart(world, pos.up(), player);
			} else if (state.get(HALF) == BlockHalf.TOP) {
				destroyPart(world, pos.down(), player);
			}
		}

		super.onBreak(world, pos, state, player);
	}

	private void destroyPart(World world, BlockPos blockPos, PlayerEntity player)
	{
		BlockState blockState = world.getBlockState(blockPos);
		if (blockState.getBlock() instanceof BlockTatooineHomeDoor || blockState.getBlock() instanceof BlockTatooineHomeDoorController)
		{
			world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 35);
			world.syncWorldEvent(player, 2001, blockPos, Block.getRawIdFromState(blockState));
		}
	}

	public static class Item extends BlockItem
	{
		private final BlockTatooineHomeDoor block;

		public Item(BlockTatooineHomeDoor block, net.minecraft.item.Item.Settings settings)
		{
			super(block, settings);
			this.block = block;
		}

		@Override
		protected boolean place(ItemPlacementContext context, BlockState state)
		{
			return block.canPlace(context.getWorld(), context.getBlockPos(), state, context.getPlayer()) && super.place(context, state);
		}
	}
}
