package me.fzzyhmstrs.amethyst_core.scepter_util

import net.minecraft.util.Formatting

/**
 * Defines the school of magic the augment is in. Schools of magic increment different scepter statistics and have separete level requirements for application and usage.
 *
 * Recommendations for setting the spell type
 *
 * [FURY]: Spells of damage and conflict. Typically used with damaging spells and/or spells that apply negative effects to opponents.
 *
 * [GRACE]: Spells of healing and protection. Used with healing spells, fortification or shielding spells, or other types of spells that focus on providing beneficial effects.
 *
 * [WIT]: Spells of mind magic and trickery. Used with spells that alter the world or the players place in it. Magic of trickery. Many summoning and block-placing spells are wit, or something like a Slow Time spell.
 *
 * [NULL]: Not recommended for use. A null spell will not affect any scepter stat, and a scepter cannot have any level info for null spell types. Only used for Magic Missile, the base spell in Amethyst Imbuement.
 */
enum class SpellType {
    FURY{
        override fun str(): String {
            return "fury"
        }

        override fun fmt(): Formatting {
            return Formatting.RED
        }
    },
    GRACE{
        override fun str(): String {
            return "grace"
        }

        override fun fmt(): Formatting {
            return Formatting.GREEN
        }
    },
    WIT{
        override fun str(): String {
            return "wit"
        }

        override fun fmt(): Formatting {
            return Formatting.BLUE
        }
    },
    NULL{
        override fun str(): String {
            return "null"
        }

        override fun fmt(): Formatting {
            return Formatting.WHITE
        }
    };

    abstract fun str(): String
    abstract fun fmt(): Formatting
}