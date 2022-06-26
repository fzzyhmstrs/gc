package me.fzzyhmstrs.amethyst_core.registry

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.item_util.AbstractScepterItem
import me.fzzyhmstrs.amethyst_core.misc_util.PersistentEffectHelper
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterHelper
import me.fzzyhmstrs.amethyst_core.trinket_util.BaseAugment
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.util.Identifier

object EventRegistry {

    private val TICKER_EVENT = Identifier(AC.MOD_ID, "ticker_event")
    private val QUEUE_TICK_EVENT = Identifier(AC.MOD_ID, "queue_tick_event")
    val ticker_40 = Ticker(40)
    val ticker_30 = Ticker(30)
    val ticker_20 = Ticker(20)

    fun registerAll(){
        registerServerTick()
        SyncedConfigPacketRegistry.registerServer()
        ScepterHelper.registerServer()
        //PlaceItemAugment.registerServer()
    }

    fun registerClient(){
        SyncedConfigPacketRegistry.registerClient()
        AbstractScepterItem.registerClient()
    }

    private fun registerServerTick(){
        ServerTickEvents.START_SERVER_TICK.register(TICKER_EVENT) {
            ticker_40.tickUp()
            ticker_30.tickUp()
            ticker_20.tickUp()
            ScepterHelper.tickModifiers()
            PersistentEffectHelper.persistentEffectTicker()
        }
        ServerTickEvents.END_SERVER_TICK.register(QUEUE_TICK_EVENT) {
            if (BaseAugment.checkEffectsQueue()){
                BaseAugment.applyEffects()
            }
        }

    }

    class Ticker(private val reset: Int = 20){
        private var tick = 1
        private var ready = false

        fun tickUp(){
            if (tick == reset) {
                tick = 1
                ready = true
                return
            }
            ready = false
            tick++
        }

        fun isReady(): Boolean{
            return ready
        }
        fun isNotReady(): Boolean{
            return !ready
        }
    }


}