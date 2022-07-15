package me.fzzyhmstrs.amethyst_core.coding_util

import me.fzzyhmstrs.amethyst_core.coding_util.PlayerParticles.scepterParticlePos
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
@Deprecated("PlayerParticlesV2 tracks head pitch, this does not. Use is fine, but suboptimal")
@Environment(value = EnvType.CLIENT)
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

    fun playerParticlePos(pos: Vec3d, width: Float, yaw: Float, offset: Vec3d): Vec3d {
        val posX = pos.x - (width + offset.x) * 0.5 * MathHelper.sin(yaw * (Math.PI.toFloat() / 180)) - offset.z * MathHelper.cos(yaw * (Math.PI.toFloat() / 180))
        val posY = pos.y + offset.y
        val posZ = pos.z + (width + offset.x) * 0.5 * MathHelper.cos(yaw * (Math.PI.toFloat() / 180)) - offset.z * MathHelper.sin(yaw * (Math.PI.toFloat() / 180))
        return Vec3d(posX, posY, posZ)
    }
}