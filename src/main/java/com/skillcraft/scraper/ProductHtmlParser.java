package com.skillcraft.scraper;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTML;
import javax.swing.text.html.parser.ParserDelegator;

/**
 * Extracts schema.org Product cards from a catalog HTML page using Java's
 * built-in tolerant HTML parser. It is intentionally tailored to pages that
 * expose product name, price, and rating in HTML rather than browser-rendered
 * JavaScript.
 */
final class ProductHtmlParser extends HTMLEditorKit.ParserCallback {
    private enum Field { NONE, NAME, PRICE }

    private final URI sourceUrl;
    private final List<Product> products = new ArrayList<>();
    private ProductBuilder current;
    private int nestingDepth;
    private int productStartDepth;
    private Field activeField = Field.NONE;
    private StringBuilder fieldText = new StringBuilder();

    private ProductHtmlParser(URI sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    static List<Product> parse(String html, URI sourceUrl) throws IOException {
        ProductHtmlParser callback = new ProductHtmlParser(sourceUrl);
        new ParserDelegator().parse(new StringReader(html), callback, true);
        return List.copyOf(callback.products);
    }

    @Override
    public void handleStartTag(HTML.Tag tag, MutableAttributeSet attributes, int position) {
        nestingDepth++;

        if (current == null && tag == HTML.Tag.DIV && isProductCard(attributes)) {
            current = new ProductBuilder();
            productStartDepth = nestingDepth;
        }

        if (current == null) {
            return;
        }

        String itemProperty = attribute(attributes, "itemprop");
        if ("name".equalsIgnoreCase(itemProperty)) {
            beginField(Field.NAME);
            String href = attribute(attributes, "href");
            if (href != null && !href.isBlank()) {
                current.productUrl = sourceUrl.resolve(href.trim()).toString();
            }
        } else if ("price".equalsIgnoreCase(itemProperty)) {
            beginField(Field.PRICE);
        } else if ("priceCurrency".equalsIgnoreCase(itemProperty)) {
            current.currency = firstNonBlank(attribute(attributes, "content"), attribute(attributes, "value"));
        }

        String rating = attribute(attributes, "data-rating");
        if (rating != null && !rating.isBlank()) {
            current.rating = rating.trim();
        }
    }

    @Override
    public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet attributes, int position) {
        if (current == null) {
            return;
        }

        String itemProperty = attribute(attributes, "itemprop");
        if ("priceCurrency".equalsIgnoreCase(itemProperty)) {
            current.currency = firstNonBlank(attribute(attributes, "content"), attribute(attributes, "value"));
        }
    }

    @Override
    public void handleText(char[] data, int position) {
        if (current != null && activeField != Field.NONE) {
            fieldText.append(data);
        }
    }

    @Override
    public void handleEndTag(HTML.Tag tag, int position) {
        if (current != null && activeField != Field.NONE) {
            saveActiveField();
        }

        if (current != null && tag == HTML.Tag.DIV && nestingDepth == productStartDepth) {
            Product product = current.build(sourceUrl.toString());
            if (product != null) {
                products.add(product);
            }
            current = null;
            activeField = Field.NONE;
            fieldText.setLength(0);
        }

        nestingDepth--;
    }

    private void beginField(Field field) {
        saveActiveField();
        activeField = field;
        fieldText.setLength(0);
    }

    private void saveActiveField() {
        String value = normalize(fieldText.toString());
        if (!value.isEmpty()) {
            if (activeField == Field.NAME) {
                current.name = value;
            } else if (activeField == Field.PRICE) {
                current.price = value;
            }
        }
        activeField = Field.NONE;
        fieldText.setLength(0);
    }

    private static boolean isProductCard(MutableAttributeSet attributes) {
        String itemType = attribute(attributes, "itemtype");
        return itemType != null && itemType.toLowerCase().contains("schema.org/product");
    }

    private static String attribute(MutableAttributeSet attributes, String name) {
        Enumeration<?> names = attributes.getAttributeNames();
        while (names.hasMoreElements()) {
            Object key = names.nextElement();
            if (name.equalsIgnoreCase(String.valueOf(key))) {
                Object value = attributes.getAttribute(key);
                return value == null ? null : String.valueOf(value);
            }
        }
        return null;
    }

    private static String normalize(String value) {
        return value.replaceAll("\\s+", " ").trim();
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.trim();
        }
        return second == null ? "" : second.trim();
    }

    private static final class ProductBuilder {
        private String name = "";
        private String price = "";
        private String currency = "";
        private String rating = "";
        private String productUrl = "";

        private Product build(String sourceUrl) {
            if (name.isBlank() || price.isBlank()) {
                return null;
            }
            return new Product(name, price, currency, rating, productUrl, sourceUrl);
        }
    }
}
