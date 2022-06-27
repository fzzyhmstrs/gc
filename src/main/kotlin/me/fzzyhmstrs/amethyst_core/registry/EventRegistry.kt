package me.fzzyhmstrs.amethyst_core.registry

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.coding_util.Dustbin
import me.fzzyhmstrs.amethyst_core.item_util.AbstractScepterItem
import me.fzzyhmstrs.amethyst_core.coding_util.PersistentEffectHelper
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterHelper
import me.fzzyhmstrs.amethyst_core.trinket_util.EffectQueue
import me.fzzyhmstrs.amethyst_core.trinket_util.base_augments.BaseAugment
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Identifier
import java.util.function.Consumer

object EventRegistry {

    private val TICKER_EVENT = Identifier(AC.MOD_ID, "ticker_event")
    private val QUEUE_TICK_EVENT = Identifier(AC.MOD_ID, "queue_tick_event")
    val ticker_40 = Ticker(40)
    val ticker_30 = Ticker(30)
    val ticker_20 = Ticker(20)
    private val tickers: MutableList<TickUppable> = mutableListOf()

    fun registerTickUppable(ticker: Ticker){
        tickers.add(ticker)
    }

    internal fun registerAll(){
        registerTickUppable(ticker_20)
        registerTickUppable(ticker_30)
        registerTickUppable(ticker_40)
        registerServerTick()
        SyncedConfigRegistry.registerServer()
        ScepterHelper.registerServer()
        //PlaceItemAugment.registerServer()
    }

    internal fun registerClient(){
        SyncedConfigRegistry.registerClient()
        AbstractScepterItem.registerClient()
    }

    private fun registerServerTick(){
        ServerTickEvents.START_SERVER_TICK.register(TICKER_EVENT) {
            tickers.forEach {
                it.tickUp()
            }
            PersistentEffectHelper.persistentEffectTicker()
        }
        ServerTickEvents.END_SERVER_TICK.register(QUEUE_TICK_EVENT) {
            if (EffectQueue.checkEffectsQueue()){
                EffectQueue.applyEffects()
            }
        }
    }

    open class Ticker(private val reset: Int = 20): TickUppable{
        private var tick = 1
        private var ready = false

        override fun tickUp(){
            if (tick == reset) {
                tick = 1
                ready = true
                ready()
                return
            }
            ready = false
            tick++
        }
        
        open fun ready(){
        }

        fun isReady(): Boolean{
            return ready
        }
        fun isNotReady(): Boolean{
            return !ready
        }
    }
    
    interface TickUppable{
        fun tickUp()
    }
}
