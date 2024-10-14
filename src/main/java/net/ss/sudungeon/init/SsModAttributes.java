package net.ss.sudungeon.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.ss.sudungeon.SsMod;

import java.util.Optional;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class SsModAttributes {

    public static final DeferredRegister<Attribute> REGISTRY = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, SsMod.MOD_ID);

    // Đăng ký các thuộc tính
    public static final RegistryObject<Attribute> SSSMO = REGISTRY.register("sssmo", () ->
            new RangedAttribute("attribute.ss.sssmo", 1, 0, 1000).setSyncable(true));
    public static final RegistryObject<Attribute> CRITICAL_HIT_CHANCE = REGISTRY.register("critical_hit_chance", () ->
            new RangedAttribute("attribute.ss.critical_hit_chance", 0.1, 0, 1.0).setSyncable(true));
    public static final RegistryObject<Attribute> MAX_MANA = REGISTRY.register("max_mana", () ->
            new RangedAttribute("attribute.ss.max_mana", 20.0, 0, 1024).setSyncable(true));
    public static final RegistryObject<Attribute> MAX_STAMINA = REGISTRY.register("max_stamina", () ->
            new RangedAttribute("attribute.ss.max_stamina", 20.0, 0, 1024).setSyncable(true));
    public static final RegistryObject<Attribute> CRITICAL_HIT_DAMAGE_MULTIPLE = REGISTRY.register("critical_hit_damage_multiple", () ->
            new RangedAttribute("attribute.ss.critical_hit_damage_multiple", 1.5, 0, 1024).setSyncable(true));
    public static final RegistryObject<Attribute> ATTACK_REACH = REGISTRY.register("attack_reach", () ->
            new RangedAttribute("attribute.ss.attack_reach", 2.5, 0, 1024).setSyncable(true));

    @SubscribeEvent
    public static void addAttributes (EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, SSSMO.get());
        event.add(EntityType.PLAYER, CRITICAL_HIT_CHANCE.get());
        event.add(EntityType.PLAYER, MAX_MANA.get());
        event.add(EntityType.PLAYER, MAX_STAMINA.get());
        event.add(EntityType.PLAYER, CRITICAL_HIT_DAMAGE_MULTIPLE.get());
        event.add(EntityType.PLAYER, ATTACK_REACH.get());
    }

    @Mod.EventBusSubscriber
    public static class PlayerAttributesSync {
        @SubscribeEvent
        public static void playerClone (PlayerEvent.Clone event) {
            if (event.isWasDeath()) {
                Player oldPlayer = event.getOriginal();
                Player newPlayer = event.getEntity();

                // Sao chép thuộc tính với phương thức hỗ trợ
                copyAttribute(oldPlayer, newPlayer, CRITICAL_HIT_CHANCE.get(), "CRITICAL_HIT_CHANCE");
                copyAttribute(oldPlayer, newPlayer, MAX_MANA.get(), "MAX_MANA");
                copyAttribute(oldPlayer, newPlayer, MAX_STAMINA.get(), "MAX_STAMINA");
                copyAttribute(oldPlayer, newPlayer, CRITICAL_HIT_DAMAGE_MULTIPLE.get(), "CRITICAL_HIT_DAMAGE_MULTIPLE");
            }
        }

        private static void copyAttribute (Player oldPlayer, Player newPlayer, Attribute attribute, String attributeName) {
            Optional.ofNullable(newPlayer.getAttribute(attribute))
                    .ifPresent(newAttr -> Optional.ofNullable(oldPlayer.getAttribute(attribute))
                            .ifPresent(oldAttr -> newAttr.setBaseValue(oldAttr.getBaseValue())));

            if (newPlayer.getAttribute(attribute) == null || oldPlayer.getAttribute(attribute) == null) {
                System.out.println(attributeName + " attribute is missing in new or old player");
            }
        }
    }
}
