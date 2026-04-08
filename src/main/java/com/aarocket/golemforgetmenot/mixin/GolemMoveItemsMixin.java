package com.aarocket.golemforgetmenot.mixin;

import com.aarocket.golemforgetmenot.GolemForgetMeNotConfig;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;

@Mixin(TransportItemsBetweenContainers.class)
public class GolemMoveItemsMixin {
	// Change the limit, basically modify all instances of the constant 10 in the markVisited function to instead check with the modifyVisits function
	@ModifyConstant(
			method = "markVisited(Lnet/minecraft/entity/mob/PathAwareEntity;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V",
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


	// extra height when chest searching
	// Default search is 0.5 vertically, increase for every block you want
	@ModifyConstant(
			method = "isWithinRange",
			constant = @Constant(doubleValue = 0.5)
	)
	private double modifyWithinRange(double original)
	{
		return 0.5  + (GolemForgetMeNotConfig.getHeightReach() - 2);
	}


	// Sometimes higher chests werent being detected, shift golem position only for raycast to make it easier to see higher chests
	// this kinda breaks with anything higher than 4
	@ModifyVariable(
			method = "isVisible",
			at = @At("HEAD"),
			ordinal = 0
	)
	private Vec3d modifyVecPos(Vec3d original)
	{
		// adjust to middle of chests
		// any value 2 or greater doesnt detect bottom blocks
		return original.add(0,1.5,0);
	}
}