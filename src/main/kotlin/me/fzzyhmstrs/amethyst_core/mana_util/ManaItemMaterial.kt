package me.fzzyhmstrs.amethyst_core.mana_util

/**
 * tool material to pair with a [ManaItem]
 */
interface ManaItemMaterial {

    fun minCooldown(): Long{
        return 20L
    }
    fun baseCooldown(): Long{
        return 150L
    }
    fun healCooldown(): Long
}
