package me.fzzyhmstrs.amethyst_core.mana_util

interface ManaItemMaterial {

    fun minCooldown(): Long{
        return 20L
    }
    fun baseCooldown(): Long{
        return 150L
    }
    fun healCooldown(): Long
}
