package com.parzivail.util.item;

import net.minecraft.item.ItemConvertible;
import net.minecraft.nbt.CompoundTag;

public interface IDefaultNbtProvider
{
	CompoundTag getDefaultTag(ItemConvertible item, int count);
}
