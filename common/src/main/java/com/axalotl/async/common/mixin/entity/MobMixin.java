package com.axalotl.async.common.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.locks.ReentrantLock;

//Yarn Name: Mob.class
@Mixin(Mob.class)
public class MobMixin {
	@Unique
	private static final ReentrantLock async$lock = new ReentrantLock();

	@WrapMethod(method = "equipItemIfPossible")
	private ItemStack tryEquip(ItemStack stack, Operation<ItemStack> original) {
		synchronized (async$lock) {
			return original.call(stack);
		}
	}

	@WrapMethod(method = "setItemSlot")
	private void equipStack(EquipmentSlot slot, ItemStack stack, Operation<Void> original) {
		synchronized (async$lock) {
			original.call(slot, stack);
		}
	}

	@WrapMethod(method = "setItemSlotAndDropWhenKilled")
	private void equipLootStack(EquipmentSlot slot, ItemStack stack, Operation<Void> original) {
		synchronized (async$lock) {
			original.call(slot, stack);
		}
	}

	@WrapMethod(method = "setBodyArmorItem")
	private void equipLootStack(ItemStack stack, Operation<Void> original) {
		synchronized (async$lock) {
			original.call(stack);
		}
	}
}