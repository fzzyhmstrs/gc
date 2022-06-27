package me.fzzyhmstrs.amethyst_core.item_util

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.coding_util.PlayerParticles.scepterParticlePos
import me.fzzyhmstrs.amethyst_core.nbt_util.Nbt
import me.fzzyhmstrs.amethyst_core.nbt_util.NbtKeys
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterHelper
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterToolMaterial
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ToolItem
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.text.MutableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.UseAction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

abstract class AbstractScepterItem(material: ScepterToolMaterial, settings: Settings, vararg defaultModifier: Identifier): ToolItem(material, settings), ManaItem {

    private val tickerManaRepair: Long
    protected val defaultModifiers: MutableList<Identifier> = mutableListOf()
    abstract val fallbackId: Identifier

    init {
        tickerManaRepair = material.healCooldown()
        defaultModifier.forEach {
            defaultModifiers.add(it)
        }
    }

    fun getRepairTime(): Int{
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

    open fun defaultActiveEnchant(): Identifier{
        return fallbackId
    }
    
    //removes cooldown on the item if you switch item
    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        if (world.isClient) return
        if (entity !is PlayerEntity) return
        //slowly heal damage over time
        if (ScepterHelper.tickTicker(stack)){
            healDamage(1,stack)
        }
    }

    companion object{
       
        fun writeDefaultNbt(stack: ItemStack){
            val nbt = stack.orCreateNbt
            val item = stack.item
            if (item is AbstractScepterItem) {
                if(!nbt.contains(NbtKeys.ACTIVE_ENCHANT.str())){
                    val identifier = item.fallbackId
                    Nbt.writeStringNbt(NbtKeys.ACTIVE_ENCHANT.str(), identifier.toString(), nbt)
                }
                val nbtList = NbtList()
                item.defaultModifiers.forEach {
                    val nbtEl = NbtCompound()
                    Nbt.writeStringNbt(NbtKeys.MODIFIER_ID.str(),it.toString(), nbtEl)
                    nbtList.add(nbtEl)
                }
                nbt.put(NbtKeys.MODIFIERS.str(),nbtList)
            }
            ScepterHelper.getScepterStats(stack)
        }
        fun addDefaultEnchantment(stack: ItemStack){
            val item = stack.item
            if (item is AbstractScepterItem) {
                val enchantToAdd = Registry.ENCHANTMENT.get(item.fallbackId)
                if (enchantToAdd != null){
                    if (EnchantmentHelper.getLevel(enchantToAdd,stack) == 0){
                        stack.addEnchantment(enchantToAdd,1)
                    }
                }
            }
        }
    }

}
