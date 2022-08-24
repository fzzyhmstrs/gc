package me.fzzyhmstrs.amethyst_core.entity_util

import net.minecraft.entity.LivingEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World
import java.util.*

interface PlayerCreatable {

    var maxAge: Int
    var createdBy: UUID?
    var owner: LivingEntity?

    fun writePlayerCreatedNbt(nbt: NbtCompound){
        nbt.putInt("MaxAge", maxAge)
        if (createdBy != null) {
            nbt.putUuid("owner",createdBy)
        }
    }

    fun readPlayerCreatedNbt(world: World, nbt: NbtCompound){
        maxAge = nbt.getInt("MaxAge")
        if (nbt.contains("owner")){
            val uuid = nbt.getUuid("owner")
            createdBy = uuid
            if (world is ServerWorld) {
                val chkEntity = world.getEntity(createdBy)
                if (chkEntity is LivingEntity) {
                    owner = chkEntity
                }
            }
        }
    }
}