package com.parzivail.pswg.entity.ship;

import com.parzivail.pswg.Client;
import com.parzivail.pswg.Resources;
import com.parzivail.pswg.client.camera.ChaseCam;
import com.parzivail.pswg.client.input.ShipControls;
import com.parzivail.pswg.container.SwgPackets;
import com.parzivail.pswg.entity.data.TrackedDataHandlers;
import com.parzivail.pswg.util.QuatUtil;
import com.parzivail.util.entity.IFlyingVehicle;
import com.parzivail.util.math.MathUtil;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

public abstract class ShipEntity extends Entity implements IFlyingVehicle
{
	private static final TrackedData<Quaternion> ROTATION = DataTracker.registerData(ShipEntity.class, TrackedDataHandlers.QUATERNION);
	private static final TrackedData<Float> THROTTLE = DataTracker.registerData(ShipEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Short> CONTROLS = DataTracker.registerData(ShipEntity.class, TrackedDataHandlers.SHORT);

	@Environment(EnvType.CLIENT)
	private ChaseCam camera;

	@Environment(EnvType.CLIENT)
	private Quaternion clientInstRotation = new Quaternion(Quaternion.IDENTITY);
	@Environment(EnvType.CLIENT)
	private Quaternion clientRotation = new Quaternion(Quaternion.IDENTITY);
	@Environment(EnvType.CLIENT)
	private Quaternion clientPrevRotation = new Quaternion(Quaternion.IDENTITY);
	@Environment(EnvType.CLIENT)
	private boolean firstRotationUpdate = true;

	private Quaternion viewRotation = new Quaternion(Quaternion.IDENTITY);
	private Quaternion viewPrevRotation = new Quaternion(Quaternion.IDENTITY);

	public ShipEntity(EntityType<?> type, World world)
	{
		super(type, world);
		this.inanimate = true;
	}

	public static void handleFirePacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
	{
		server.execute(() -> {
			ShipEntity ship = getShip(player);

			if (ship != null)
				ship.acceptFireInput();
		});
	}

	public static void handleRotationPacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
	{
		float qa = buf.readFloat();
		float qb = buf.readFloat();
		float qc = buf.readFloat();
		float qd = buf.readFloat();

		server.execute(() -> {
			ShipEntity ship = getShip(player);

			if (ship != null)
				ship.setRotation(new Quaternion(qb, qc, qd, qa));
		});
	}

	public static void handleControlPacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender)
	{
		short controls = buf.readShort();

		server.execute(() -> {
			ShipEntity ship = getShip(player);

			if (ship != null)
				ship.acceptControlInput(ShipControls.unpack(controls));
		});
	}

	public static ShipEntity getShip(PlayerEntity player)
	{
		Entity vehicle = player.getVehicle();

		if (vehicle instanceof ShipEntity)
		{
			ShipEntity ship = (ShipEntity)vehicle;

			if (ship.getPrimaryPassenger() == player)
				return ship;
		}

		return null;
	}

	@Override
	public Box getVisibilityBoundingBox()
	{
		return getBoundingBox().expand(5);
	}

	@Override
	protected boolean canClimb()
	{
		return false;
	}

	protected float getEyeHeight(EntityPose pose, EntityDimensions dimensions)
	{
		return getHeight() / 2f;
	}

	@Override
	public boolean isPushable()
	{
		return true;
	}

	@Override
	public boolean collides()
	{
		return !this.removed;
	}

	@Override
	public boolean damage(DamageSource source, float amount)
	{
		if (this.isInvulnerableTo(source))
			return false;

		this.kill();

		return true;
	}

	@Override
	protected void initDataTracker()
	{
		getDataTracker().startTracking(ROTATION, new Quaternion(Quaternion.IDENTITY));
		getDataTracker().startTracking(THROTTLE, 0f);
		getDataTracker().startTracking(CONTROLS, (short)0);
	}

	@Override
	protected void readCustomDataFromTag(CompoundTag tag)
	{
		if (tag.contains("rotation"))
			setRotation(QuatUtil.getQuaternion(tag.getCompound("rotation")));
		setThrottle(tag.getFloat("throttle"));
	}

	@Override
	protected void writeCustomDataToTag(CompoundTag tag)
	{
		CompoundTag qTag = new CompoundTag();
		QuatUtil.putQuaternion(qTag, getRotation());
		tag.put("rotation", qTag);

		tag.putFloat("throttle", getThrottle());
	}

	@Environment(EnvType.CLIENT)
	public ChaseCam getCamera()
	{
		if (camera == null)
		{
			camera = new ChaseCam();
		}

		return camera;
	}

	@Override
	public void tick()
	{
		viewPrevRotation = new Quaternion(viewRotation);

		super.tick();
		if (this.isLogicalSideForUpdatingMovement())
			this.updateTrackedPosition(this.getX(), this.getY(), this.getZ());

		viewRotation = new Quaternion(getRotation());

		if (world.isClient)
		{
			if (Client.isShipClientControlled(this))
			{
				clientPrevRotation = new Quaternion(clientRotation);
				clientRotation = new Quaternion(clientInstRotation);
			}
			else
			{
				clientPrevRotation = new Quaternion(viewPrevRotation);
				clientRotation = new Quaternion(viewRotation);
			}

			ChaseCam camera = getCamera();
			camera.tick(this);
		}

		float throttle = getThrottle();

		Entity pilot = getPrimaryPassenger();
		if (pilot instanceof PlayerEntity)
		{
			EnumSet<ShipControls> controls = getControls();

			if (controls.contains(ShipControls.THROTTLE_UP))
				throttle += 0.3f;
			if (controls.contains(ShipControls.THROTTLE_DOWN))
				throttle -= 0.3f;

			throttle = MathHelper.clamp(throttle, 0, 3);

			setThrottle(throttle);
		}
		else if (throttle > 0)
		{
			if (throttle > 0.5f)
				throttle *= 0.8f;
			else
				throttle = 0;

			throttle = MathHelper.clamp(throttle, 0, 3);
			setThrottle(throttle);
		}

		Vec3d forward = QuatUtil.rotate(MathUtil.NEGZ, getRotation());
		setVelocity(forward.multiply(throttle));

		move(MovementType.SELF, getVelocity());

		QuatUtil.updateEulerRotation(this, getRotation());
	}

	public ActionResult interact(PlayerEntity player, Hand hand)
	{
		if (player.shouldCancelInteraction())
			return ActionResult.FAIL;
		else
			return !this.world.isClient && player.startRiding(this) ? ActionResult.CONSUME : ActionResult.FAIL;
	}

	protected boolean canAddPassenger(Entity passenger)
	{
		return this.getPassengerList().size() < 2;
	}

	public void updatePassengerPosition(Entity passenger)
	{
		if (this.hasPassenger(passenger))
		{
			Vec3d vec3d = new Vec3d(0, 0, 3 * this.getPassengerList().indexOf(passenger));
			vec3d = QuatUtil.rotate(vec3d, getRotation());

			passenger.updatePosition(this.getX() + vec3d.x, this.getY() + vec3d.y, this.getZ() + vec3d.z);
			this.copyEntityData(passenger);
		}
	}

	protected void copyEntityData(Entity entity)
	{
		entity.yaw = this.yaw;
		entity.pitch = this.pitch;
	}

	@Nullable
	public Entity getPrimaryPassenger()
	{
		List<Entity> list = this.getPassengerList();
		return list.isEmpty() ? null : list.get(0);
	}

	//	public Rotation getRotation(float t)
	//	{
	//		Rotation start = prevRotation;
	//		Rotation end = getRotation();
	//
	//		return MathUtil.lerp(start, end, t);
	//	}

	@Override
	public abstract Packet<?> createSpawnPacket();

	public EnumSet<ShipControls> getControls()
	{
		return ShipControls.unpack(getDataTracker().get(CONTROLS));
	}

	public void setControls(EnumSet<ShipControls> controls)
	{
		getDataTracker().set(CONTROLS, ShipControls.pack(controls));
	}

	public float getThrottle()
	{
		return getDataTracker().get(THROTTLE);
	}

	public void setThrottle(float t)
	{
		if (t != getThrottle())
			getDataTracker().set(THROTTLE, t);
	}

	public Quaternion getRotation()
	{
		return getDataTracker().get(ROTATION);
	}

	public void setRotation(Quaternion q)
	{
		QuatUtil.normalize(q);
		getDataTracker().set(ROTATION, q);
	}

	@Environment(EnvType.CLIENT)
	public Quaternion getViewRotation(float t)
	{
		Quaternion start = clientPrevRotation;
		Quaternion end = clientRotation;
		return QuatUtil.slerp(start, end, t);
	}

	public void acceptControlInput(EnumSet<ShipControls> controls)
	{
		if (ShipControls.pack(controls) == getDataTracker().get(CONTROLS))
			return;

		setControls(controls);

		if (this.world.isClient)
		{
			PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
			passedData.writeShort(ShipControls.pack(controls));
			ClientPlayNetworking.send(SwgPackets.C2S.PacketShipControls, passedData);
		}
	}

	public void acceptFireInput()
	{
	}

	@Environment(EnvType.CLIENT)
	public void acceptMouseInput(double mouseDx, double mouseDy)
	{
		if (this.firstUpdate)
			return;

		Quaternion rotation = new Quaternion(clientInstRotation);
		if (firstRotationUpdate)
		{
			rotation = new Quaternion(getRotation());
			firstRotationUpdate = false;
		}

		boolean shipRollPriority = Resources.CONFIG.get().input.shipRollPriority;

		if (Client.KEY_SHIP_INPUT_MODE_OVERRIDE.isPressed())
			shipRollPriority = !shipRollPriority;

		if (shipRollPriority)
			rotation.hamiltonProduct(new Quaternion(new Vector3f(0, 0, 1), -(float)mouseDx * 0.15f, true));
		else
		{
			Vec3d v = QuatUtil.project(com.parzivail.util.math.MathUtil.POSY, rotation);
			rotation.hamiltonProduct(new Quaternion(new Vector3f(v), (float)(Math.asin(v.y) * -mouseDx * 0.1f), true));

			// TODO: roll back toward zero when this mode is switched to and the ship has a nonzero roll
		}

		rotation.hamiltonProduct(new Quaternion(new Vector3f(1, 0, 0), -(float)mouseDy * 0.1f, true));

		setRotation(rotation);

		clientInstRotation = new Quaternion(rotation);

		PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
		passedData.writeFloat(rotation.getW());
		passedData.writeFloat(rotation.getX());
		passedData.writeFloat(rotation.getY());
		passedData.writeFloat(rotation.getZ());
		ClientPlayNetworking.send(SwgPackets.C2S.PacketShipRotation, passedData);
	}

	public void acceptLeftClick()
	{
		ClientPlayNetworking.send(SwgPackets.C2S.PacketShipFire, new PacketByteBuf(Unpooled.buffer()));
	}
}
