package com.parzivail.pswg;

import com.parzivail.util.noise.OpenSimplex2F;
import me.shedaniel.autoconfig.ConfigHolder;
import net.minecraft.util.Identifier;

import javax.annotation.Nonnull;
import java.util.Random;

public class Resources
{
	public static final String MODID = "pswg";
	public static final String NAME = "Galaxies: Parzi's Star Wars Mod";
	public static final boolean IS_DEBUG = System.getenv("PSWG_DEBUG") != null && Boolean.parseBoolean(System.getenv("PSWG_DEBUG"));

	public static final OpenSimplex2F SIMPLEX_0 = new OpenSimplex2F(0);
	public static final Random RANDOM = new Random();
	public static ConfigHolder<Config> CONFIG;

	public static Identifier identifier(@Nonnull String path)
	{
		return new Identifier(MODID, path);
	}

	public static String container(String str)
	{
		return "container." + MODID + "." + str;
	}

	public static String command(String str)
	{
		return "command." + MODID + "." + str;
	}

	public static String keyBinding(String str)
	{
		return "key." + MODID + "." + str;
	}
}
