package com.aarocket.golemforgetmenot.mixin;

import com.aarocket.golemforgetmenot.GolemForgetMeNotConfig;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.Container;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.TransportItemsBetweenContainers;
import net.minecraft.world.entity.ai.behavior.TransportItemsBetweenContainers.TransportItemTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(TransportItemsBetweenContainers.class)
public abstract class GolemMoveItemsMixin {
	@Shadow
	protected abstract boolean canSeeAnyTargetSide(final TransportItemTarget target, final Level level, final PathfinderMob body, final Vec3 eyePosition);

	// Change the limit, basically modify all instances of the constant 10 in the markVisited function to instead check with the modifyVisits function
	@ModifyConstant(
			method = "setVisitedBlockPos(Lnet/minecraft/world/entity/PathfinderMob;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V",
			constant = @Constant(intValue = 10)
	)
	private int modifyVisits(int original) {
		return GolemForgetMeNotConfig.getVisitsUntilCooldown();
	}

	// If configuration asks for it, ignore empty slots during first pass of insertion, focussing on completing existing stacks first
    @ModifyExpressionValue(
        method = "addItemsToContainer(Lnet/minecraft/world/entity/PathfinderMob;Lnet/minecraft/world/Container;)Lnet/minecraft/world/item/ItemStack;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 0)
    )
    private static boolean modifyEmptyCheck(boolean original) {
        // Only treat it as empty if we're NOT in complete-stacks mode
        return original && !GolemForgetMeNotConfig.getCompleteStacks();
    }
	
	// If configuration asks for it, only insert in empty slots after trying to complete existing stacks
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


	// Extra height when chest searching
	// Default search is 0.5 vertically, increase for every block you want
	@ModifyConstant(
			method = "isWithinTargetDistance",
			constant = @Constant(doubleValue = 0.5)
	)
	private double modifyWithinTargetDistance(double original)
	{
		return 0.5  + (GolemForgetMeNotConfig.getHeightReach() - 2);
	}

	// Sometimes higher chests werent being detected, add one more raycast with shifted golem position so in can see higher chests
	@Inject(
			method = "targetIsReachableFromPosition",
			at = @At("RETURN"),
			cancellable = true
	)
	private void targetIsReachableFromPosition(Level level, boolean canReachTarget, Vec3 pos, TransportItemTarget target, PathfinderMob body, CallbackInfoReturnable<Boolean> cir) {
		boolean reachable = cir.getReturnValue();
		if (reachable || !canReachTarget || GolemForgetMeNotConfig.getHeightReach() == 2) return;
		// Adjust to middle of chests, and raycast again to check if we can see the chest from there
		cir.setReturnValue(canReachTarget && this.canSeeAnyTargetSide(target, level, body, pos.add(0,1.5,0)));
   	}
}