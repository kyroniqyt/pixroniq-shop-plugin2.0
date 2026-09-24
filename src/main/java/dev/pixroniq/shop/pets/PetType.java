package dev.pixroniq.shop.pets;

import org.bukkit.entity.EntityType;

/**
 * NOTE on CAPYBARA: there is no capybara entity in vanilla Minecraft on Java or Bedrock.
 * It's mapped to a Turtle here as a working placeholder so purchase/follow/despawn logic all
 * function correctly - it will just visually be a turtle until a resource pack that reskins
 * one is installed. Dog, Cat, and Parrot are real vanilla mobs and render correctly on both
 * platforms through Geyser with no extra work.
 */
public enum PetType {
    DOG(EntityType.WOLF, "Dog"),
    CAT(EntityType.CAT, "Cat"),
    PARROT(EntityType.PARROT, "Parrot"),
    CAPYBARA(EntityType.TURTLE, "Capybara");

    private final EntityType entityType;
    private final String label;

    PetType(EntityType entityType, String label) {
        this.entityType = entityType;
        this.label = label;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public String getLabel() {
        return label;
    }

    public static PetType parse(String text) {
        try {
            return PetType.valueOf(text.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
