package com.parzivail.swg.weapon;

import com.parzivail.swg.StarWarsGalaxy;
import com.parzivail.swg.entity.EntityBlasterBolt;
import com.parzivail.swg.item.ICustomCrosshair;
import com.parzivail.swg.item.ILeftClickInterceptor;
import com.parzivail.swg.item.PItem;
import com.parzivail.swg.weapon.blastermodule.BlasterData;
import com.parzivail.swg.weapon.blastermodule.BlasterScopes;
import com.parzivail.swg.weapon.blastermodule.IBlasterScope;
import com.parzivail.util.audio.SoundHandler;
import com.parzivail.util.common.AnimatedValue;
import com.parzivail.util.entity.EntityUtils;
import com.parzivail.util.math.Ease;
import com.parzivail.util.math.RaytraceHit;
import com.parzivail.util.math.RaytraceHitBlock;
import com.parzivail.util.math.RaytraceHitEntity;
import com.parzivail.util.ui.Fx;
import com.parzivail.util.ui.gltk.EnableCap;
import com.parzivail.util.ui.gltk.GL;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class ItemBlasterRifle extends PItem implements ICustomCrosshair, ILeftClickInterceptor
{
	public final float damage;
	public final float spread;
	public final int maxDistance;
	public final int maxClipSize;
	public final int boltColor;

	private AnimatedValue avExpansion;

	public ItemBlasterRifle(String name, float damage, float spread, int maxDistance, int maxClipSize, int boltColor)
	{
		super("rifle." + name);
		this.damage = damage;
		this.spread = spread;
		this.maxDistance = maxDistance;
		this.maxClipSize = maxClipSize;
		this.boltColor = boltColor;
		this.setCreativeTab(CreativeTabs.tabCombat);
		this.maxStackSize = 1;

		avExpansion = new AnimatedValue(-2, 100);
	}

	@Override
	public boolean isFull3D()
	{
		return true;
	}

	@Override
	public boolean shouldRequestRenderState(ItemStack stack, World world, EntityPlayer player)
	{
		return true;
	}

	@Override
	public boolean shouldUsePrecisionMovement(ItemStack stack, World world, EntityPlayer player)
	{
		BlasterData bd = new BlasterData(stack);
		return bd.isAimingDownSights;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		BlasterData bd = new BlasterData(stack);

		if (player.isSneaking())
		{
			bd.shotsRemaining = maxClipSize;
			if (!world.isRemote)
				SoundHandler.playSound((EntityPlayerMP)player, "pswg:swg.fx.rifleReload", player.posX, player.posY, player.posZ, 1, 1);

			bd.scope++;
			bd.scope %= BlasterScopes.NUM_SCOPES;
		}
		else
			bd.isAimingDownSights = !bd.isAimingDownSights;

		bd.serialize(stack.stackTagCompound);
		return stack;
	}

	@Override
	public float getZoomLevel(ItemStack stack, World world, EntityPlayer player)
	{
		BlasterData bd = new BlasterData(stack);
		IBlasterScope scope = BlasterScopes.getScope(bd.scope);
		if (scope != null)
			return scope.getZoomLevel();
		return 0.4f;
	}

	@Override
	public void drawCrosshair(ScaledResolution sr, EntityPlayer player, ItemStack stack)
	{
		float expansion = 32 * avExpansion.animateTo(getSpreadAmount(stack, player), Ease::outQuad) + 5;
		BlasterData bd = new BlasterData(stack);
		Minecraft mc = Minecraft.getMinecraft();

		float size = 2;

		if (bd.isAimingDownSights)
		{
			GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
			GL.Enable(EnableCap.LineSmooth);
			IBlasterScope scope = BlasterScopes.getScope(bd.scope);
			if (scope != null)
				scope.draw(sr, player, stack);
		}
		else
		{
			GL11.glLineWidth(4);
			GL11.glColor4f(0, 0, 0, 1);
			Fx.D2.DrawLine(0, expansion, 0, size + expansion);
			Fx.D2.DrawLine(0, -expansion, 0, -size - expansion);
			Fx.D2.DrawLine(expansion, 0, size + expansion, 0);
			Fx.D2.DrawLine(-expansion, 0, -size - expansion, 0);

			GL11.glLineWidth(2);
			GL11.glColor4f(1, 1, 1, 1);
			Fx.D2.DrawLine(0, expansion, 0, size + expansion);
			Fx.D2.DrawLine(0, -expansion, 0, -size - expansion);
			Fx.D2.DrawLine(expansion, 0, size + expansion, 0);
			Fx.D2.DrawLine(-expansion, 0, -size - expansion, 0);
		}

		GL.Enable(EnableCap.Texture2D);
		GL.PushMatrix();
		GL.Translate(sr.getScaledWidth_double() / 2, sr.getScaledHeight_double() / 2, 0);

		String remaining = String.format("%s/%s", bd.shotsRemaining, maxClipSize);
		int w = mc.fontRenderer.getStringWidth(remaining);
		int h = mc.fontRenderer.FONT_HEIGHT;

		GL.Translate(-w - 1, -h - 1, 0);
		mc.fontRenderer.drawString(remaining, 0, 0, 0xFFFFFF);

		GL.PopMatrix();
	}

	private float getSpreadAmount(ItemStack stack, EntityPlayer player)
	{
		if (spread == 0)
			return 0;

		BlasterData bd = new BlasterData(stack);
		if (bd.isAimingDownSights)
			return 0;

		double movement = Math.sqrt(player.moveForward * player.moveForward + player.moveStrafing * player.moveStrafing);
		return spread * (0.5f * (float)movement + 0.05f);
	}

	@Override
	public void onItemLeftClick(ItemStack stack, World world, EntityPlayer player)
	{
		BlasterData bd = new BlasterData(stack);
		if (bd.shotsRemaining <= 0)
		{
			if (!world.isRemote)
				SoundHandler.playSound((EntityPlayerMP)player, "pswg:swg.fx.rifleDryfire", player.posX, player.posY, player.posZ, 1, 1);
			return;
		}

		if (!world.isRemote)
		{
			float spread = getSpreadAmount(stack, player);
			Vec3 look = player.getLook(0);
			look.xCoord += (world.rand.nextFloat() * 2 - 1) * spread;
			look.yCoord += (world.rand.nextFloat() * 2 - 1) * spread;
			look.zCoord += (world.rand.nextFloat() * 2 - 1) * spread;
			RaytraceHit hit = EntityUtils.rayTrace(look, maxDistance, player, new Entity[0], true);

			SoundHandler.playSound((EntityPlayerMP)player, "pswg:swg.fx." + name, player.posX, player.posY, player.posZ, 1 + (float)world.rand.nextGaussian() / 10, 1);

			Entity e = new EntityBlasterBolt(world, (float)look.xCoord, (float)look.yCoord, (float)look.zCoord, damage, boltColor);
			e.setPosition(player.posX, player.posY + player.getEyeHeight(), player.posZ);
			world.spawnEntityInWorld(e);

			if (hit instanceof RaytraceHitEntity && ((RaytraceHitEntity)hit).entity instanceof EntityLiving)
			{
				EntityLiving entity = (EntityLiving)((RaytraceHitEntity)hit).entity;
				entity.attackEntityFrom(DamageSource.causePlayerDamage(player), damage);
			}

			if (hit instanceof RaytraceHitBlock)
			{
				RaytraceHitBlock block = (RaytraceHitBlock)hit;
				for (int i = 0; i < 10; i++)
					StarWarsGalaxy.proxy.spawnParticle(world, "smoke", block.hitVec.xCoord + (world.rand.nextDouble() * 0.2 - 0.1), block.hitVec.yCoord + (world.rand.nextDouble() * 0.2 - 0.1), block.hitVec.zCoord + (world.rand.nextDouble() * 0.2 - 0.1), 0, world.rand.nextDouble() * 0.2, 0);
			}
		}

		bd.shotsRemaining--;

		bd.serialize(stack.stackTagCompound);

		// Recoil
		player.rotationPitch -= damage / 2;
		player.rotationYaw += damage / 20 * world.rand.nextGaussian();
	}
}