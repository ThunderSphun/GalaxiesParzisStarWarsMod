package com.parzivail.util.common;

import com.parzivail.util.math.Ease;
import com.parzivail.util.ui.Fx;

import java.util.function.Function;

public class AnimatedValue
{
	private float previous;
	private float next;
	private long nextTime;

	private final int msToTake;

	public AnimatedValue(float value, int msToTake)
	{
		this.previous = value;
		this.next = value;
		this.msToTake = msToTake;
	}

	public float animateTo(float value)
	{
		return animateTo(value, Ease::linear);
	}

	public float animateTo(float value, Function<Float, Float> interpolation)
	{
		long timeHere = Fx.Util.GetMillis();

		if (value != next)
		{
			previous = next;
			next = value;

			nextTime = timeHere + msToTake;
		}

		if (timeHere > nextTime)
			return next;

		long timeDiff = (nextTime - timeHere);
		float timeLerp = interpolation.apply(timeDiff / (float)msToTake);

		return next * (1 - timeLerp) + previous * timeLerp;
	}
}