/*
package net.ss.sudungeon.mixin;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.spongepowered.asm.mixin.*;

@Mixin(Attributes.class)
public class AttributesMixin {

    // Chúng ta thay thế giá trị của MAX_HEALTH
    @Shadow
    @Final
    @Mutable
    public static Attribute MAX_HEALTH;

    // Chúng ta thay thế giá trị của ATTACK_DAMAGE
    @Shadow
    @Final
    @Mutable
    public static Attribute ATTACK_DAMAGE;

    // Thêm các thuộc tính mới cho Mana và Stamina
    @Unique
    @Final
    @Mutable
    private static Attribute MANA;


    @Unique
    @Final
    @Mutable
    private static Attribute STAMINA;

    static {
        // Khởi tạo lại giá trị cho MAX_HEALTH và ATTACK_DAMAGE
        MAX_HEALTH = new RangedAttribute("attribute.name.generic.max_health", 20.0D, 1.0D, 2048.0D).setSyncable(true);
        ATTACK_DAMAGE = new RangedAttribute("attribute.name.generic.attack_damage", 2.0D, 0.0D, 4096.0D);

        // Khởi tạo giá trị mặc định cho MANA và STAMINA
        MANA = new RangedAttribute("attribute.name.generic.mana", 100.0D, 0.0D, 2048.0D).setSyncable(true);
        STAMINA = new RangedAttribute("attribute.name.generic.stamina", 100.0D, 0.0D, 2048.0D).setSyncable(true);
    }
}
*/
