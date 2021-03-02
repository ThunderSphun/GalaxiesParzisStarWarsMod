package com.parzivail.util.scarif;

import com.google.common.io.LittleEndianDataInputStream;
import com.parzivail.util.Lumberjack;
import com.parzivail.util.binary.DataReader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

public class ScarifChunk
{
	public final HashMap<BlockPos, CompoundTag> tiles = new HashMap<>();
	private final LittleEndianDataInputStream stream;
	public int numSections;
	private boolean initialized = false;

	public ScarifChunk(LittleEndianDataInputStream stream)
	{
		this.stream = stream;
	}

	public void init()
	{
		if (initialized)
		{
			Lumberjack.error("SCARIF chunk cannot initialize twice");
			return;
		}

		try
		{
			int numTiles = DataReader.read7BitEncodedInt(stream);

			for (int i = 0; i < numTiles; i++)
			{
				int x = DataReader.read7BitEncodedInt(stream);
				int y = DataReader.read7BitEncodedInt(stream);
				int z = DataReader.read7BitEncodedInt(stream);

				int nbtLen = stream.readInt();
				CompoundTag nbt = DataReader.readUncompressedNbt(stream, nbtLen);

				tiles.put(new BlockPos(x, y, z), nbt);
			}

			numSections = DataReader.read7BitEncodedInt(stream);

			initialized = true;
		}
		catch (IOException e)
		{
			Lumberjack.error("SCARIF chunk failed to load tiles");
			e.printStackTrace();
		}
	}

	public ScarifSection readSection()
	{
		try
		{
			int y = stream.readByte();

			int paletteSize = DataReader.read7BitEncodedInt(stream);
			BlockState[] palette = new BlockState[paletteSize];

			for (int i = 0; i < paletteSize; i++)
				palette[i] = readBlockState();

			int[] blockStates = new int[4096];
			for (int i = 0; i < blockStates.length; i++)
			{
				blockStates[i] = DataReader.read7BitEncodedInt(stream);
			}

			return new ScarifSection(y, palette, blockStates);
		}
		catch (IOException e)
		{
			Lumberjack.error("SCARIF chunk failed to load section");
			e.printStackTrace();
		}

		return null;
	}

	public BlockState readBlockState() throws IOException
	{
		String name = DataReader.readNullTerminatedString(stream);
		boolean hasProperties = stream.readByte() == 1;

		DefaultedRegistry<Block> blockRegistry = Registry.BLOCK;
		Block block = blockRegistry.get(new Identifier(name));
		BlockState blockState = block.getDefaultState();

		if (hasProperties)
		{
			StateManager<Block, BlockState> stateManager = block.getStateManager();

			int tagLen = stream.readInt();
			CompoundTag props = DataReader.readUncompressedNbt(stream, tagLen);

			for (String key : props.getKeys())
			{
				Property<? extends Comparable<?>> property = stateManager.getProperty(key);
				if (property != null)
					blockState = withProperty(blockState, property, key, props, blockState.toString());
			}
		}

		return blockState;
	}

	private static <T extends Comparable<T>> BlockState withProperty(BlockState state, net.minecraft.state.property.Property<T> property, String key, CompoundTag propertiesTag, String context)
	{
		Optional<T> optional = property.parse(propertiesTag.getString(key));
		if (optional.isPresent())
			return state.with(property, optional.get());
		else
		{
			Lumberjack.warn("Unable to read property: %s with value: %s for blockstate: %s", key, propertiesTag.getString(key), context);
			return state;
		}
	}
}
