package com.aarocket.golemforgetmenot.mixin;

import com.aarocket.golemforgetmenot.GolemForgetMeNotConfig;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import net.minecraft.world.Container;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.TransportItemsBetweenContainers;
import net.minecraft.world.item.ItemStack;

@Mixin(TransportItemsBetweenContainers.class)
public class GolemMoveItemsMixin {
	// change the limit, basically modify all instances of the constant 10 in the markVisited function to instead check with the modifyVisits function
	@ModifyConstant(
			method = "setVisitedBlockPos(Lnet/minecraft/world/entity/PathfinderMob;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V",
			constant = @Constant(intValue = 10)
	)
	private int modifyVisits(int original) {
		return GolemForgetMeNotConfig.getVisitsUntilCooldown();
	}

	// if configuration asks for it, ignore empty slots during first pass of insertion, focussing on completing existing stacks first
    @ModifyExpressionValue(
        method = "addItemsToContainer(Lnet/minecraft/world/entity/PathfinderMob;Lnet/minecraft/world/Container;)Lnet/minecraft/world/item/ItemStack;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 0)
    )
    private static boolean modifyEmptyCheck(boolean original) {
        // Only treat it as empty if we're NOT in complete-stacks mode
        return original && !GolemForgetMeNotConfig.getCompleteStacks();
    }
	
	// if configuration asks for it, only insert in empty slots after trying to complete existing stacks
    @Inject(
        method = "addItemsToContainer(Lnet/minecraft/world/entity/PathfinderMob;Lnet/minecraft/world/Container;)Lnet/minecraft/world/item/ItemStack;",
        at = @At("RETURN"),
        cancellable = true
    )
    private static void addSecondPass(PathfinderMob entity, Container inventory, CallbackInfoReturnable<ItemStack> cir) {
        if (!GolemForgetMeNotConfig.getCompleteStacks()) return;
        ItemStack itemStack = cir.getReturnValue();
        if (itemStack.isEmpty()) return;
        int i = 0;
        for (Iterator<ItemStack> it = inventory.iterator(); it.hasNext(); ++i) {
            ItemStack itemStack2 = it.next();
            if (itemStack2.isEmpty()) {
                inventory.setItem(i, itemStack);
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }
        }
        // If we reach here, itemStack still not inserted, return it as-is
        cir.setReturnValue(itemStack);
    }

}