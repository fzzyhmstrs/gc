package me.fzzyhmstrs.amethyst_core.scepter_util

interface ScepterMaterialAddon {

    fun minCooldown(): Long{
        return 20L
    }
    fun baseCooldown(): Long{
        return 150L
    }
    fun healCooldown(): Long

    fun scepterTier(): Int
}
