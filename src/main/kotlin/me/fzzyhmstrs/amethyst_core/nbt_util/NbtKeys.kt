package me.fzzyhmstrs.amethyst_core.nbt_util

enum class NbtKeys {

    TOTEM{
        override fun str(): String {
            return "totem_active"
        }
    },
    ANGELIC{
        override fun str(): String {
            return "angelic"
        }
    },
    ALTAR_KEY{
        override fun str(): String {
            return "altar_used"
        }
    },
    SCEPTER_ID{
        override fun str(): String {
            return "scepter_id"
        }
    },
    ITEM_STACK_ID{
        override fun str(): String {
            return "item_stack_id"
        }
    },
    LORE_KEY{
        override fun str(): String {
            return "book_of_lore_augment"
        }
    },
    ACTIVE_ENCHANT{
        override fun str(): String {
            return "active_enchant_id"
        }
    },
    LAST_USED{
        override fun str(): String {
            return "_last_used"
        }
    },
    LAST_USED_LIST{
        override fun str(): String {
            return "last_used_list"
        }
    },
    DISENCHANT_COUNT{
        override fun str(): String {
            return "disenchant_count"
        }
    },
    MODIFIERS{
        override fun str(): String {
            return "modifiers"
        }
    },
    MODIFIER_ID{
        override fun str(): String {
            return "modifier_id"
        }
    },
    LOCK_POS{
        override fun str(): String {
            return "lock_pos"
        }
    },
    LOCKS{
        override fun str(): String {
            return "switch_locks"
        }
    },
    DOORS{
        override fun str(): String {
            return "switch_doors"
        }
    },
    DOOR_POS{
        override fun str(): String {
            return "door_pos"
        }
    },
    KEY_ITEM{
        override fun str(): String {
            return "key_item"
        }
    },
    HELD_ITEM{
        override fun str(): String {
            return "held_item"
        }
    },
    KEY_NUM{
        override fun str(): String {
            return "key_num"
        }
    },
    PORTAL_KEY{
        override fun str(): String {
            return "portal_key"
        }
    },
    FRAME_LIST{
        override fun str(): String {
            return "frame_list"
        }
    },
    FRAME_POS{
        override fun str(): String {
            return "frame_pos"
        }
    },
    PORTAL_LIST{
        override fun str(): String {
            return "portal_list"
        }
    },
    PORTAL_POS{
        override fun str(): String {
            return "portal_pos"
        }
    };

    abstract fun str(): String
}