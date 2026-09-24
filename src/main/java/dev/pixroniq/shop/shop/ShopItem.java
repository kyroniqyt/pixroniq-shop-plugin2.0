package dev.pixroniq.shop.shop;

public class ShopItem {

    private final String id;
    private final ShopCategory category;
    private String displayName;
    private int price;
    /**
     * Meaning depends on category:
     *  COLOR       - a ChatColor name (e.g. AQUA) or a legacy code letter (e.g. b)
     *  PREFIX      - the literal prefix text, & color codes allowed
     *  PET         - a PetType name (DOG, CAT, PARROT, CAPYBARA)
     *  KILLMESSAGE - a message template using {killer} and {victim}
     */
    private String value;

    public ShopItem(String id, ShopCategory category, String displayName, int price, String value) {
        this.id = id;
        this.category = category;
        this.displayName = displayName;
        this.price = price;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public ShopCategory getCategory() {
        return category;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
