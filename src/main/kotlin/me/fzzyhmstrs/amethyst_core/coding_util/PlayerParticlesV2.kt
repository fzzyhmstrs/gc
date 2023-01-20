package me.fzzyhmstrs.amethyst_core.coding_util

import me.fzzyhmstrs.amethyst_core.coding_util.PlayerParticlesV2.scepterParticlePos
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.Perspective
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d

/**
 * utility for locating and creating particles that track a specific spot in the players field of veiw.
 *
 * The built-in implementation [scepterParticlePos] below positions and emits particles where the tip of a "handheld" type items ends. This gives the illusion that particles are being emitted directly from the item itself.
 *
 * See the [DefaultScepterItem][me.fzzyhmstrs.amethyst_core.item_util.DefaultScepterItem] method doSmoke for a simple example implementation that causes smoke to puff out of the end of a scepter when a spell fails.
 *
 * As seen below, the particles are perspective-dependent, so particles properly render in 1st or 3rd person.
 *
 * For your own implementation, follow along with how the scepterParticlePos is developed. The basic technique is 1) locate the player in space (first scepterParticlPos), 2) define the positional offsets needed (second scepterParticlePos), 3) apply that data to the playerParticlePos function.
 */

@Environment(value = EnvType.CLIENT)
object PlayerParticlesV2 {
    fun scepterParticlePos(client: MinecraftClient, user: LivingEntity): Vec3d {
        val perspective = client.options.perspective
        val pos = user.getCameraPosVec(client.tickDelta)
        val rot = getRotVec(user, client, perspective)
        val yaw = getYaw(user, client, perspective)
        val pitch = getPitch(user, perspective)
        val width = user.width
        val fov = MathHelper.clamp(client.options.fov,30.0,110.0)
        val offsets = scepterOffset(perspective, fov)
        return playerParticlePos(pos, rot,yaw, pitch, width, offsets)
    }

    /*private fun scepterParticlePos(pos: Vec3d, width: Float, yaw: Float, pitch: Float, perspective: Perspective, fov: Double): Vec3d {
        val offset: Vec3d = scepterOffset(perspective, fov)
        return playerParticlePos(pos, width, yaw, pitch, offset)
    }*/

    fun scepterOffset(perspective: Perspective, fov: Double): Pair<Double, Double> {
        return when(perspective){
            Perspective.FIRST_PERSON -> {
                val fpx = MathHelper.lerpFromProgress(fov,30.0,110.0,0.8,-0.2)
                Pair(fpx, -0.05)
            }
            Perspective.THIRD_PERSON_FRONT -> {
                Pair(1.0, -0.5)
            }
            Perspective.THIRD_PERSON_BACK -> {
                Pair(1.0, -0.5)
            }
        }
    }

    fun playerParticlePos(pos: Vec3d, rot: Vec3d, yaw: Float, pitch: Float, width: Float, offsets: Pair<Double,Double>): Vec3d {
        val perpendicularVector = perpendicularVecFromYaw(yaw)
        val downwardVector = downwardOffsetVec(yaw, pitch, offsets.second.toFloat())
        return pos.add(rot.multiply(offsets.first)).add(perpendicularVector.multiply(width / 2.0)).add(downwardVector)
    }

    private fun vecFromYaw(yaw: Float): Vec3d{
        val posX = MathHelper.sin(-yaw * (Math.PI.toFloat() / 180)).toDouble()
        val posY = 0.0
        val posZ = MathHelper.cos(-yaw * (Math.PI.toFloat() / 180)).toDouble()
        return Vec3d(posX, posY, posZ)
    }

    private fun downwardOffsetVec(yaw: Float, pitch: Float, offset: Float): Vec3d{
        val yawVec = vecFromYaw(yaw)
        val posX = offset * yawVec.x * MathHelper.sin(pitch * (Math.PI.toFloat() / 180)).toDouble()
        val posY = offset * MathHelper.cos(pitch * (Math.PI.toFloat() / 180)).toDouble()
        val posZ = offset * yawVec.z * MathHelper.sin(pitch * (Math.PI.toFloat() / 180)).toDouble()
        return Vec3d(posX, posY, posZ)
    }

    private fun perpendicularVecFromYaw(yaw: Float): Vec3d{
        val posX = -1.0 * MathHelper.cos(yaw * (Math.PI.toFloat() / 180)).toDouble()
        val posY = 0.0
        val posZ = MathHelper.sin(-yaw * (Math.PI.toFloat() / 180)).toDouble()
        return Vec3d(posX, posY, posZ)
    }

    fun getYaw(user: LivingEntity,client: MinecraftClient, perspective: Perspective): Float{
        return if(perspective == Perspective.THIRD_PERSON_FRONT || perspective == Perspective.THIRD_PERSON_BACK){
            user.bodyYaw
        } else {
            user.getYaw(client.tickDelta)
        }
    }

    fun getPitch(user: LivingEntity, perspective: Perspective): Float{
        return if(perspective == Perspective.FIRST_PERSON){
            user.pitch
        } else {
            0.0F
        }
    }

    fun getRotVec(user: LivingEntity,client: MinecraftClient,perspective: Perspective): Vec3d{
        return if(perspective == Perspective.FIRST_PERSON){
            user.getRotationVec(client.tickDelta)
        } else {
            val yaw = getYaw(user, client, perspective)
            vecFromYaw(yaw)
        }
    }
}