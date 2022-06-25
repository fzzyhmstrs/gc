package me.fzzyhmstrs.amethyst_core.scepter_util

import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.Perspective
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d

object PlayerParticles {
    fun scepterParticlePos(client: MinecraftClient, user: LivingEntity): Vec3d {
        val pos = user.getCameraPosVec(client.tickDelta)
        val width = user.width
        val perspective = client.options.perspective
        val yaw = if(perspective == Perspective.THIRD_PERSON_FRONT){
            user.bodyYaw
        } else {
            user.getYaw(client.tickDelta)
        }
        val fov = MathHelper.clamp(client.options.fov,30.0,110.0)
        return scepterParticlePos(pos, width, yaw, perspective, fov)
    }

    private fun scepterParticlePos(pos: Vec3d, width: Float, yaw: Float, perspective: Perspective, fov: Double): Vec3d {
        val offset: Vec3d = scepterOffset(perspective, fov)
        return playerParticlePos(pos, width, yaw, offset)
    }

    fun playerParticlePos(pos: Vec3d, width: Float, yaw: Float, offset: Vec3d): Vec3d {
        val posX = pos.x - (width + offset.x) * 0.5 * MathHelper.sin(yaw * (Math.PI.toFloat() / 180)) - offset.z * MathHelper.cos(yaw * (Math.PI.toFloat() / 180))
        val posY = pos.y + offset.y
        val posZ = pos.z + (width + offset.x) * 0.5 * MathHelper.cos(yaw * (Math.PI.toFloat() / 180)) - offset.z * MathHelper.sin(yaw * (Math.PI.toFloat() / 180))
        return Vec3d(posX, posY, posZ)
    }

    fun scepterOffset(perspective: Perspective, fov: Double): Vec3d {
        return when(perspective){
            Perspective.FIRST_PERSON -> {
                val fpx = MathHelper.lerpFromProgress(fov,30.0,110.0,1.4,-0.1)
                Vec3d(fpx,-0.13,0.6)
            }
            Perspective.THIRD_PERSON_FRONT -> {
                Vec3d(0.8,-0.5,0.5)
            }
            Perspective.THIRD_PERSON_BACK -> {
                Vec3d(0.8, -0.5, 0.5)
            }
        }
    }
}