package com.skillcraft.scraper;

/** A product record ready to be written to CSV. */
public record Product(
        String name,
        String price,
        String currency,
        String rating,
        String productUrl,
        String sourceUrl) {
}
