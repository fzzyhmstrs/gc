package me.fzzyhmstrs.amethyst_core.coding_util

/**
 * Simple interface for building a class that can be added together in some way
 *
 * Can be as simple as some sort of primitive implementation or an extensive data class that you want to be able to combine
 */

interface Addable<T>{
    fun plus(other: T): T
}