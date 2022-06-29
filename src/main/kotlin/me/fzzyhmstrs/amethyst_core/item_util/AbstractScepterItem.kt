package me.fzzyhmstrs.amethyst_core.item_util

import me.fzzyhmstrs.amethyst_core.mana_util.ManaHelper
import me.fzzyhmstrs.amethyst_core.mana_util.ManaItem
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterToolMaterial
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.*
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World

abstract class AbstractScepterItem(material: ScepterToolMaterial, settings: Settings): CustomFlavorToolItem(material, settings), ManaItem {

    private val tickerManaRepair: Long = material.healCooldown()
    abstract val fallbackId: Identifier

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        val nbt = stack.orCreateNbt
        if (needsInitialization(stack, nbt)){
            initializeScepter(stack, nbt)
        }
        return super.use(world, user, hand)
    }

    override fun getRepairTime(): Int{
        return tickerManaRepair.toInt()
    }

    override fun isFireproof(): Boolean {
        return true
    }

    override fun getItemBarColor(stack: ItemStack): Int {
        return MathHelper.hsvToRgb(0.66f,1.0f,1.0f)
    }

    override fun getUseAction(stack: ItemStack): UseAction {
        return UseAction.BLOCK
    }

    override fun onCraft(stack: ItemStack, world: World, player: PlayerEntity) {
        val nbt = stack.orCreateNbt
        writeDefaultNbt(stack, nbt)
        initializeScepter(stack, nbt)
    }

    open fun writeDefaultNbt(stack: ItemStack, scepterNbt: NbtCompound){
    }

    open fun initializeScepter(stack: ItemStack, scepterNbt: NbtCompound){
        ManaHelper.initializeManaItem(stack)
    }

    open fun needsInitialization(stack: ItemStack, scepterNbt: NbtCompound): Boolean{
        return false
    }
    
    //removes cooldown on the item if you switch item
    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        if (world.isClient) return
        //slowly heal damage over time
        if (ManaHelper.tickHeal(stack)){
            healDamage(1,stack)
        }
    }
}
