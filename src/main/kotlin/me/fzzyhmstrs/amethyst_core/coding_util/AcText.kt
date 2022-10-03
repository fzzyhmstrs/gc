package me.fzzyhmstrs.amethyst_core.coding_util


import import net.minecraft.text.Text
import import net.minecraft.text.MutableText

class AcText(){

    fun translatable(key: String, vararg args: Object): MutableText{
        return Text.translatable(key, *args)
    }
    
    fun literal(text: String): MutableText{
        return Text.literal(text)
    }
    
    fun empty(): MutableText{
        return Text.empty()
    }
    
    fun appended(baseText: MutableText, vararg appenders: Text): MutableText{
        val finalText = baseText
        appenders.forEach{
            finalText.append(it)
        }
        return finalText
    }
}
