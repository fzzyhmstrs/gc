package me.fzzyhmstrs.amethyst_core.registry

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.coding_util.PersistentEffectHelper
import me.fzzyhmstrs.amethyst_core.item_util.interfaces.ParticleEmitting
import me.fzzyhmstrs.amethyst_core.registry.EventRegistry.Ticker
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterHelper
import me.fzzyhmstrs.amethyst_core.trinket_util.EffectQueue
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.util.Identifier

/**
 * registry for tick-timed events. Event monitoring is achieved with [Ticker]s that can be ticked and queried for readiness.
 *
 * Three pre-registered tickers are available by default for 1, 1.5, and 2 second intervals.
 *
 * Tickers like these are useful for processing events at less than per-tick rates, which can save a lot of processing power and help prevent lag, as well as make certain things like application of status effects less annoying.
 */
object EventRegistry {

    private val TICKER_EVENT = Identifier(AC.MOD_ID, "ticker_event")
    private val QUEUE_TICK_EVENT = Identifier(AC.MOD_ID, "queue_tick_event")
    val ticker_40 = Ticker(40)
    val ticker_30 = Ticker(30)
    val ticker_20 = Ticker(20)
    private val tickers: MutableList<TickUppable> = mutableListOf()

    /**
     * register a [Ticker] or other type of class that extends the [TickUppable] interface with this method. The registry will tick tickUp() every tick, and the class can determine what to do with that clock signal.
     */
    fun registerTickUppable(ticker: TickUppable){
        tickers.add(ticker)
    }
    fun removeTickUppable(ticker: TickUppable){
        if (ticker == ticker_20) return
        if (ticker == ticker_30) return
        if (ticker == ticker_40) return
        tickers.remove(ticker)
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
        ParticleEmitting.registerClient()
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

    /**
     * basic ticker. Registry ticks these every tick, and the ticker shows as ready ever "reset" number of ticks.
     *
     * If extended [ready] can be used to execute some code automatically each time the ticker resets.
     */
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

    /**
     * interface for creation of classes that can be registered with the Event Registry for ticking that aren't an extension of a ticker.
     */
    interface TickUppable{
        fun tickUp()
    }
}
