package com.parzivail.pswg.block;

import com.parzivail.pswg.blockentity.TatooineHomeDoorBlockEntity;
import com.parzivail.util.block.VoxelShapeUtil;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

import java.util.EnumMap;

public class BlockTatooineHomeDoorController extends BlockTatooineHomeDoor implements BlockEntityProvider
{
	private static final VoxelShape INTERACTION_SHAPE_CLOSED = VoxelShapes.union(
			VoxelShapes.cuboid(0, 0, 0.25, 1, 0.0625, 0.75),
			VoxelShapes.cuboid(0, 0, 0.25, 0.0625, 1, 0.75),
			VoxelShapes.cuboid(1 - 0.0625, 0, 0.25, 1, 1, 0.75),
			VoxelShapes.cuboid(0.0625, 0.0625, 0.375, 1 - 0.0625, 1, 0.625));
	private static final VoxelShape INTERACTION_SHAPE_OPEN = VoxelShapes.union(
			VoxelShapes.cuboid(0, 0, 0.25, 1, 0.0625, 0.75),
			VoxelShapes.cuboid(0, 0, 0.25, 0.0625, 1, 0.75),
			VoxelShapes.cuboid(1 - 0.0625, 0, 0.25, 1, 1, 0.75),
			VoxelShapes.cuboid(1 - 1.5 * 0.0625, 0.0625, 0.375, 1 - 0.0625, 1, 0.625));
	private static final VoxelShape COLLISION_SHAPE_CLOSED = VoxelShapes.union(
			VoxelShapes.cuboid(0, 0, 0.25, 0.0625, 1, 0.75),
			VoxelShapes.cuboid(1 - 0.0625, 0, 0.25, 1, 1, 0.75),
			VoxelShapes.cuboid(0.0625, 0.0625, 0.375, 1 - 0.0625, 1, 0.625));
	private static final VoxelShape COLLISION_SHAPE_OPEN = VoxelShapes.union(
			VoxelShapes.cuboid(0, 0, 0.25, 0.0625, 1, 0.75),
			VoxelShapes.cuboid(1 - 0.0625, 0, 0.25, 1, 1, 0.75),
			VoxelShapes.cuboid(1 - 1.5 * 0.0625, 0.0625, 0.375, 1 - 0.0625, 1, 0.625));

	protected static final EnumMap<Direction, VoxelShape> INTERACTION_SHAPES_CLOSED = new EnumMap<>(Direction.class);
	protected static final EnumMap<Direction, VoxelShape> INTERACTION_SHAPES_OPEN = new EnumMap<>(Direction.class);
	protected static final EnumMap<Direction, VoxelShape> COLLISION_SHAPES_CLOSED = new EnumMap<>(Direction.class);
	protected static final EnumMap<Direction, VoxelShape> COLLISION_SHAPES_OPEN = new EnumMap<>(Direction.class);

	static
	{
		Direction[] facingArray = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
		for (int i = 0; i < facingArray.length; i++)
		{
			INTERACTION_SHAPES_CLOSED.put(facingArray[i] , VoxelShapeUtil.rotate(INTERACTION_SHAPE_CLOSED, i));
			INTERACTION_SHAPES_OPEN.put(facingArray[i] , VoxelShapeUtil.rotate(INTERACTION_SHAPE_OPEN, i));
			COLLISION_SHAPES_CLOSED.put(facingArray[i] , VoxelShapeUtil.rotate(COLLISION_SHAPE_CLOSED, i));
			COLLISION_SHAPES_OPEN.put(facingArray[i] , VoxelShapeUtil.rotate(COLLISION_SHAPE_OPEN, i));
		}
	}

	public BlockTatooineHomeDoorController(Settings settings)
	{
		super(settings);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return getShape(state, world, pos, INTERACTION_SHAPES_OPEN, INTERACTION_SHAPES_CLOSED);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
	{
		return getShape(state, world, pos, COLLISION_SHAPES_OPEN, COLLISION_SHAPES_CLOSED);
	}

	@Override
	protected BlockPos getController(BlockView world, BlockPos self)
	{
		return self;
	}

	@Override
	public BlockEntity createBlockEntity(BlockView world)
	{
		return new TatooineHomeDoorBlockEntity();
	}
}
