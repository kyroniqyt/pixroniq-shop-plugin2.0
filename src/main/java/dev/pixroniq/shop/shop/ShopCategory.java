package dev.pixroniq.shop.shop;

public enum ShopCategory {
    COLOR,
    PREFIX,
    PET,
    KILLMESSAGE;

    public static ShopCategory parse(String text) {
        try {
            return ShopCategory.valueOf(text.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
