package me.fzzyhmstrs.amethyst_core.coding_util

import me.fzzyhmstrs.amethyst_core.registry.EventRegistry
import java.util.function.Consumer

/**
 * a lightweight "mark dirty" class that accepts members of a defined type into an internal list, and then applies a consumer to each element to "clean" the list.
 */
open class Dustbin<T>(private val consumer: Consumer<T>){
    private var dirty: Boolean = false
    private val dust: MutableList<T> = mutableListOf()
    fun markDirty(newDust: T){
        dust.add(newDust)
        dirty = true
    }
    fun isDirty(): Boolean{
        return dirty
    }
    fun clean(){
        if (!dirty) return
        val itr = dust.iterator()
        while (itr.hasNext()){
            consumer.accept(itr.next())
            itr.remove()
        }
        dirty = false
    }
}

/**
 * a ticking implementation of the Dustbin. Auto-registers itself in the [EventRegistry] and cleans itself on every tick.
 */
open class TickingDustbin<T>(consumer: Consumer<T>): Dustbin<T>(consumer), EventRegistry.TickUppable {
    
    init{
        EventRegistry.registerTickUppable(this)
    }
    
    override fun tickUp(){
        clean()
    }    
    
}
