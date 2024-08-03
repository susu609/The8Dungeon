package net.ss.sudungeon.world.entity;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.HashMap;
import java.util.Map;

public class EntityScoring {
    public static final Map<Attribute, Double> ATTRIBUTE_INVERSE_WEIGHTS = new HashMap<>();

    static {
        ATTRIBUTE_INVERSE_WEIGHTS.put(Attributes.ARMOR, 1.0 / 30.0);
        ATTRIBUTE_INVERSE_WEIGHTS.put(Attributes.ARMOR_TOUGHNESS, 1.0 / 20.0);
        ATTRIBUTE_INVERSE_WEIGHTS.put(Attributes.ATTACK_DAMAGE, 1.0 / 2048.0);
        ATTRIBUTE_INVERSE_WEIGHTS.put(Attributes.ATTACK_KNOCKBACK, 1.0 / 5.0);
        ATTRIBUTE_INVERSE_WEIGHTS.put(Attributes.ATTACK_SPEED, 1.0 / 1024.0);
        ATTRIBUTE_INVERSE_WEIGHTS.put(Attributes.FOLLOW_RANGE, 1.0 / 2048.0);
        ATTRIBUTE_INVERSE_WEIGHTS.put(Attributes.KNOCKBACK_RESISTANCE, 1.0);
        ATTRIBUTE_INVERSE_WEIGHTS.put(Attributes.MAX_HEALTH, 1.0 / 1024.0);
        ATTRIBUTE_INVERSE_WEIGHTS.put(Attributes.MOVEMENT_SPEED, 1.0 / 1024.0);

    }

    public static double calculateEntityScore (LivingEntity entity) {
        double totalWeight = ATTRIBUTE_INVERSE_WEIGHTS.entrySet().stream()
                .filter(entry -> entity.getAttributes().hasAttribute(entry.getKey()))
                .mapToDouble(entry -> {
                    double attributeValue = entity.getAttributeBaseValue(entry.getKey());
                    return attributeValue > 0 ? entry.getValue() : 0.0;
                })
                .sum();

        double score = ATTRIBUTE_INVERSE_WEIGHTS.entrySet().stream()
                .filter(entry -> entity.getAttributes().hasAttribute(entry.getKey()))
                .mapToDouble(entry -> {
                    double attributeValue = entity.getAttributeBaseValue(entry.getKey());
                    return attributeValue > 0 ? entry.getValue() / attributeValue : 0.0;
                })
                .sum();

        score /= totalWeight > 0 ? totalWeight : 1; // Tránh chia cho 0

        // Thêm điểm thưởng cho các khả năng đặc biệt (nếu có)
        score += calculateBonusScore(entity);

        return Math.round(score * 100.0) / 100.0;
    }

    public enum SpecialAbility {
        FIRE_RESISTANCE(10.0),
        FLYING(5.0),
        POISONOUS(8.0),
        INVISIBLE(12.0),
        REGENERATION(15.0);

        private final double bonusScore;

        SpecialAbility (double bonusScore) {
            this.bonusScore = bonusScore;
        }

        public double getBonusScore () {
            return bonusScore;
        }
    }

    public static boolean entityHasAbility(LivingEntity entity, SpecialAbility ability) {
        return switch (ability) {
            case FIRE_RESISTANCE -> entity.fireImmune();
            case FLYING -> entity.getAttributeBaseValue(Attributes.FLYING_SPEED) > 0;
            case POISONOUS -> entity.hasEffect(MobEffects.POISON); // Ví dụ kiểm tra hiệu ứng chất độc
            case REGENERATION -> entity.hasEffect(MobEffects.REGENERATION); // Ví dụ kiểm tra hiệu ứng hồi máu
            // ... (Thêm các kiểm tra cho các khả năng khác)
            default -> false;
        };
    }

    public static double calculateBonusScore (LivingEntity entity) {
        double bonusScore = 0.0;

        // Kiểm tra các khả năng đặc biệt từ enum
        for (SpecialAbility ability : SpecialAbility.values()) {
            if (entityHasAbility(entity, ability)) { // Triển khai phương thức entityHasAbility
                bonusScore += ability.getBonusScore();
            }
        }

        // ... (Thêm các kiểm tra cho các thuộc tính tùy chỉnh nếu cần)

        return bonusScore;
    }

}
