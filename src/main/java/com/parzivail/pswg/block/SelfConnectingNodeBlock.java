package com.parzivail.pswg.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

public class SelfConnectingNodeBlock extends ConnectingNodeBlock
{
	protected static final VoxelShape SHAPE = Block.createCuboidShape(2, 2, 2, 14, 14, 14);

	public SelfConnectingNodeBlock(Settings settings)
	{
		super(settings);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return SHAPE;
	}

	@Override
	public boolean shouldConnectTo(WorldAccess world, BlockState state, BlockState otherState, BlockPos otherPos, Direction direction)
	{
		if (state.get(FACING_PROPERTIES.get(direction)))
			return true;

		return otherState.getBlock() == this && otherState.get(FACING_PROPERTIES.get(direction.getOpposite()));
	}
}
