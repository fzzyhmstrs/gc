package me.fzzyhmstrs.amethyst_core.coding_util

import java.util.function.Consumer
import me.fzzyhmstrs.amethyst_core.registry.*

open class Dustbin<T>(private val consumer: Consumer<T>){
    protected var dirty: Boolean = false
    protected val dust: MutableList<T> = mutableListOf()
    fun markDirty(newDust: T){
        dust.add(newDust)
        dirty = true
    }
    fun isDirty(): Boolean{
        return dirty
    }
    fun clean(){
        if (!dirty) return
        dust.forEach{
            consumer.accept(it)
        }
        dust.clear()
        dirty = false
    }
}

open class TickingDustbin<T>(consumer: Consumer<T>): Dustbin<T>(consumer), EventRegistry.TickUppable {
    
    init{
        EventRegistry.registerTickUppable(this)
    }
    
    override fun tickUp(){
        clean()
    }    
    
}
