package me.fzzyhmstrs.amethyst_core.coding_util


import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.MutableText
import net.minecraft.text.TranslatableText

object AcText{

    fun translatable(key: String, vararg args: Any): MutableText{
        return TranslatableText(key, *args)
    }
    
    fun literal(text: String): MutableText{
        return LiteralText(text)
    }
    
    fun empty(): MutableText{
        return LiteralText("")
    }

    fun appended(baseText: MutableText, vararg appended: Text): MutableText {
        appended.forEach {
            baseText.append(it)
        }
        return baseText
    }
}
