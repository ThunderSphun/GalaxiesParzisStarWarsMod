package com.parzivail.util.math;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.util.math.Vector4f;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;

@Environment(EnvType.CLIENT)
public class ClientMathUtil
{
	public static final Matrix4f MATRIX_IDENTITY = new Matrix4f();

	static
	{
		MATRIX_IDENTITY.loadIdentity();
	}

	@Environment(EnvType.CLIENT)
	public static Vector3f transform(Vector3f v, Matrix4f m)
	{
		Vector4f v4 = new Vector4f(v);
		v4.transform(m);
		return new Vector3f(v4.getX(), v4.getY(), v4.getZ());
	}

	@Environment(EnvType.CLIENT)
	public static Vector3f transform(Vector3f v, Matrix3f m)
	{
		Vector3f v3 = v.copy();
		v3.transform(m);
		return v3;
	}
}
