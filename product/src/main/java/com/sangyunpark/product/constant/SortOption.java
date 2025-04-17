package com.sangyunpark.product.constant;

public enum SortOption {

    LATEST("latest"),
    PRICE_ASC("price_asc"),
    PRICE_DESC("price_desc");

    private final String value;

    SortOption(final String value) {
        this.value = value;
    }

    public static SortOption from(final String value) {
        if(value == null) return LATEST;
        for(SortOption sortOption : SortOption.values()) {
            if(sortOption.value.equals(value)) return sortOption;
        }

        return LATEST;
    }
}
