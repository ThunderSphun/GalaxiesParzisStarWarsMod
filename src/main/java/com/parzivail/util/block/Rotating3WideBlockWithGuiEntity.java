package com.parzivail.util.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class Rotating3WideBlockWithGuiEntity extends RotatingBlockWithGuiEntity
{
	public static final EnumProperty<Side> SIDE;

	static
	{
		SIDE = EnumProperty.of("side", Side.class);
	}

	public Rotating3WideBlockWithGuiEntity(Settings settings, Supplier<BlockEntity> blockEntitySupplier)
	{
		super(settings, blockEntitySupplier);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
	{
		return super.onUse(state, world, pos, player, hand, hit);
		// TODO: make all blocks access same inventory
	}

	@Override
	public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player)
	{
		super.onBreak(world, pos, state, player);

		Direction dir = state.get(FACING);
		Side side = state.get(SIDE);

		BlockPos left = pos.offset(dir.rotateYClockwise(), side.getOffset(Side.LEFT));
		BlockPos right = pos.offset(dir.rotateYClockwise(), side.getOffset(Side.RIGHT));
		BlockPos center = pos.offset(dir.rotateYClockwise(), side.getOffset(Side.MIDDLE));

		if (world.getBlockState(left).getBlock() == this)
			world.setBlockState(left, Blocks.AIR.getDefaultState());
		if (world.getBlockState(right).getBlock() == this)
			world.setBlockState(right, Blocks.AIR.getDefaultState());
		if (world.getBlockState(center).getBlock() == this)
			world.setBlockState(center, Blocks.AIR.getDefaultState());
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
	{
		Direction dir = state.get(FACING);
		BlockPos left = pos.offset(dir.rotateYCounterclockwise());
		BlockPos right = pos.offset(dir.rotateYClockwise());

		world.setBlockState(left, state.with(SIDE, Side.LEFT));
		world.setBlockState(pos, state.with(SIDE, Side.MIDDLE));
		world.setBlockState(right, state.with(SIDE, Side.RIGHT));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		super.appendProperties(builder);
		builder.add(SIDE);
	}

	public boolean canPlace(ItemPlacementContext context, BlockState state)
	{
		World world = context.getWorld();
		BlockPos pos = context.getBlockPos();
		Direction dir = state.get(FACING);
		BlockState left = world.getBlockState(pos.offset(dir.rotateYCounterclockwise()));
		BlockState right = world.getBlockState(pos.offset(dir.rotateYClockwise()));
		BlockState center = world.getBlockState(pos);
		return world.isAir(pos.offset(dir.rotateYCounterclockwise())) && world.isAir(pos.offset(dir.rotateYClockwise())) & world.isAir(pos);
	}

	public enum Side implements StringIdentifiable
	{
		LEFT("left"),
		RIGHT("right"),
		MIDDLE("middle");

		private final String name;

		Side(String name)
		{
			this.name = name;
		}

		public int getOffset(Side target)
		{
			if (target == this)
				return 0;
			switch (this)
			{
				case LEFT:
					return target == MIDDLE ? 1 : 2;
				case RIGHT:
					return target == MIDDLE ? -1 : -2;
				case MIDDLE:
					return target == LEFT ? -1 : 1;
			}
			return 0;
		}

		public Side getOpposite()
		{
			switch (this)
			{
				case LEFT:
					return RIGHT;
				case RIGHT:
					return LEFT;
				default:
					return MIDDLE;
			}
		}

		@Override
		public String asString()
		{
			return this.name;
		}
	}

	public static class Item extends BlockItem
	{

		private final Rotating3WideBlockWithGuiEntity block;

		public Item(Rotating3WideBlockWithGuiEntity block, Settings settings)
		{
			super(block, settings);
			this.block = block;
		}

		@Override
		protected boolean place(ItemPlacementContext context, BlockState state)
		{
			return this.block.canPlace(context, state) && super.place(context, state);
		}
	}
}
