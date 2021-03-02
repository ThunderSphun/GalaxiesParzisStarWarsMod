package com.parzivail.util.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class RotatingBlockWithBounds extends RotatingBlock
{
	private final VoxelShape shape;

	public RotatingBlockWithBounds(VoxelShape shape, Settings settings)
	{
		super(settings.dynamicBounds());
		this.shape = shape;
	}

	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return VoxelShapeUtil.rotate(shape, (int) state.get(FACING).asRotation() / 90);
	}
}
