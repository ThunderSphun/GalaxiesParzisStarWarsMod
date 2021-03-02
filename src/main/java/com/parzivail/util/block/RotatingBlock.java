package com.parzivail.util.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;

public class RotatingBlock extends Block
{
	public static final DirectionProperty FACING;

	static
	{
		FACING = Properties.HORIZONTAL_FACING;
	}

	public RotatingBlock(Settings settings)
	{
		super(settings);
	}

	public BlockState getPlacementState(ItemPlacementContext ctx)
	{
		return this.getDefaultState().with(FACING, ctx.getPlayerFacing());
	}

	@SuppressWarnings("deprecation")
	public BlockState rotate(BlockState state, BlockRotation rotation)
	{
		return state.with(FACING, state.get(FACING).rotateYClockwise());
	}

	@SuppressWarnings("deprecation")
	public BlockState mirror(BlockState state, BlockMirror mirror)
	{
		return state.with(FACING, state.get(FACING).getOpposite());
	}

	protected void appendProperties(StateManager.Builder<Block, BlockState> builder)
	{
		builder.add(FACING);
	}

	public float getRotationDegrees(BlockState state)
	{
		return state.get(FACING).asRotation();
	}
}
