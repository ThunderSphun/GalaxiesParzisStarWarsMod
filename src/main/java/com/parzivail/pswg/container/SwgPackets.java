package com.parzivail.pswg.container;

import com.parzivail.pswg.Resources;
import net.minecraft.util.Identifier;

public class SwgPackets
{
	public static class C2S
	{
		public static final Identifier PacketLightsaberForgeApply = Resources.identifier("lightsaber_forge_apply");
		public static final Identifier PacketSetOwnSpecies = Resources.identifier("set_own_species");
		public static final Identifier PacketPlayerLeftClickItem = Resources.identifier("player_use_left");
		public static final Identifier PacketPlayerLightsaberToggle = Resources.identifier("player_lightsaber_toggle");
		public static final Identifier PacketShipFire = Resources.identifier("ship_fire");
		public static final Identifier PacketShipRotation = Resources.identifier("ship_rotation");
		public static final Identifier PacketShipControls = Resources.identifier("ship_controls");
	}

	public static class S2C
	{
		public static final Identifier PacketSyncBlasters = Resources.identifier("sync_blasters");
	}
}
