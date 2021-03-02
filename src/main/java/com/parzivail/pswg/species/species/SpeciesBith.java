package com.parzivail.pswg.species.species;

import com.parzivail.pswg.container.SwgSpeciesRegistry;
import com.parzivail.pswg.species.SpeciesVariable;
import com.parzivail.pswg.species.SwgSpecies;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;

public class SpeciesBith extends SwgSpecies
{
	private static final SpeciesVariable VAR_BODY = new SpeciesVariable(SwgSpeciesRegistry.SPECIES_BITH,
	                                                             "body",
	                                                             "white",
	                                                             "green",
	                                                             "pink",
	                                                             "white"
	);

	public SpeciesBith(String serialized)
	{
		super(serialized);
	}

	@Override
	public Identifier getSlug()
	{
		return SwgSpeciesRegistry.SPECIES_BITH;
	}

	@Override
	public SpeciesVariable[] getVariables()
	{
		return new SpeciesVariable[] { VAR_BODY };
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Collection<Identifier> getTextureStack()
	{
		ArrayList<Identifier> stack = new ArrayList<>();
		stack.add(getGenderedTexture(this, VAR_BODY));
		stack.add(getGenderedGlobalTexture(gender, "clothes"));
		return stack;
	}
}
