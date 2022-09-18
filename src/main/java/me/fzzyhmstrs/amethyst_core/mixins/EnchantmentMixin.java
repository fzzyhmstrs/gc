package me.fzzyhmstrs.amethyst_core.mixins;

import me.fzzyhmstrs.amethyst_core.scepter_util.augments.AugmentHelper;
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin{

    @Shadow @Nullable protected String translationKey;

    @Shadow public abstract int getMaxLevel();

    @Inject(method = "getName", at = @At(value = "HEAD"), cancellable = true)
    private void disabledAugmentName(int level, CallbackInfoReturnable<Text> cir){
        Enchantment enchant = (Enchantment)(Object)this;
        if (enchant instanceof ScepterAugment) {
            Identifier id = Registry.ENCHANTMENT.getId(enchant);
            if (id != null){
                if(!AugmentHelper.INSTANCE.getAugmentEnabled(id.toString())){
                    MutableText mutableText = new TranslatableText(this.translationKey);
                    if (level != 1 || this.getMaxLevel() != 1) {
                        mutableText.append(" ").append(new TranslatableText("enchantment.level." + level));
                    }
                    mutableText.append(new TranslatableText("scepter.augment.disabled"));
                    mutableText.formatted(Formatting.DARK_RED).formatted(Formatting.STRIKETHROUGH);
                    cir.setReturnValue(mutableText);
                }
            }
        }
    }
}
