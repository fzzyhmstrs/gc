package me.fzzyhmstrs.amethyst_core.item_util

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.misc_util.PlayerParticles.scepterParticlePos
import me.fzzyhmstrs.amethyst_core.nbt_util.Nbt
import me.fzzyhmstrs.amethyst_core.nbt_util.NbtKeys
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ToolItem
import net.minecraft.item.ToolMaterial
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.Identifier
import net.minecraft.util.UseAction
import net.minecraft.util.math.MathHelper
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

abstract class AbstractScepterItem(material: ToolMaterial, settings: Settings, baseRegen: Int, vararg defaultModifier: Identifier): ToolItem(material, settings), ManaItem {

    private val tickerManaRepair: Long
    private val defaultModifiers: MutableList<Identifier> = mutableListOf()

    init {
        tickerManaRepair = if (material !is ScepterMaterialAddon){
            baseRegen.toLong()
        } else {
            material.healCooldown()
        }
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
        return Identifier("vanishing_curse")
    }

    companion object{
        val defaultId = Identifier("vanishing_curse")
        val SCEPTER_SMOKE_PACKET = Identifier(AC.MOD_ID,"scepter_smoke_packet")
        fun registerClient(){
            ClientPlayNetworking.registerGlobalReceiver(SCEPTER_SMOKE_PACKET) { minecraftClient: MinecraftClient, _, _, _ ->
                val world = minecraftClient.world
                val entity = minecraftClient.player
                if (world != null && entity != null){
                    doSmoke(world,minecraftClient,entity)
                }
            }
        }

        private fun doSmoke(world: World, client: MinecraftClient, user: LivingEntity){
            val particlePos = scepterParticlePos(client, user)
            world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,particlePos.x,particlePos.y,particlePos.z,user.velocity.x,user.velocity.y + 0.5,user.velocity.z)
        }
        private fun doSmoke(world: World, user: LivingEntity){
            val pos = user.eyePos
            val width = user.width
            val yaw = user.yaw
            val posX = pos.x - (width + 0.8f) * 0.5 * MathHelper.sin(yaw * (Math.PI.toFloat() / 180)) - 0.6 * MathHelper.cos(yaw * (Math.PI.toFloat() / 180))
            val posY = pos.y - 0.1
            val posZ = pos.z + (width + 0.8f) * 0.5 * MathHelper.cos(yaw * (Math.PI.toFloat() / 180)) - 0.6 * MathHelper.sin(yaw * (Math.PI.toFloat() / 180))
            world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,posX,posY,posZ,user.velocity.x,user.velocity.y + 0.5,user.velocity.z)
        }

        fun writeDefaultNbt(stack: ItemStack){
            val nbt = stack.orCreateNbt
            val item = stack.item
            if (item is AbstractScepterItem) {
                if(!nbt.contains(NbtKeys.ACTIVE_ENCHANT.str())){
                    val identifier = item.defaultActiveEnchant()
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
                val enchantToAdd = Registry.ENCHANTMENT.get(item.defaultActiveEnchant())
                if (enchantToAdd != null){
                    if (EnchantmentHelper.getLevel(enchantToAdd,stack) == 0){
                        stack.addEnchantment(enchantToAdd,1)
                    }
                }
            }
        }
    }

}