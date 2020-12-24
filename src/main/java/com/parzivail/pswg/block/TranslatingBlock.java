package com.parzivail.pswg.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class TranslatingBlock extends Block
{
	@FunctionalInterface
	public interface ShapeFunction
	{
		VoxelShape apply(BlockState state, BlockView world, BlockPos pos, ShapeContext context);
	}

	private final ShapeFunction shapeFunction;

	public TranslatingBlock(ShapeFunction shapeFunction, Settings settings)
	{
		super(settings.dynamicBounds());
		this.shapeFunction = shapeFunction;
	}

	public TranslatingBlock(VoxelShape shape, Settings settings)
	{
		super(settings.dynamicBounds());
		this.shapeFunction = (state, world, pos, context) -> shape;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return shapeFunction.apply(state, world, pos, context);
	}
}
