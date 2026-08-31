package com.skillcraft.scraper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Command-line e-commerce catalog scraper.
 *
 * Usage:
 * java -cp out com.skillcraft.scraper.EcommerceScraper [--url URL] [--pages NUMBER] [--output FILE]
 */
public final class EcommerceScraper {
    private static final String DEFAULT_URL =
            "https://webscraper.io/test-sites/e-commerce/static/computers/laptops";
    private static final Path DEFAULT_OUTPUT = Path.of("data", "products.csv");
    private static final Duration REQUEST_DELAY = Duration.ofMillis(700);

    private EcommerceScraper() {
    }

    public static void main(String[] args) {
        try {
            Arguments options = Arguments.parse(args);
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            List<Product> products = new ArrayList<>();
            for (int page = 1; page <= options.pages(); page++) {
                URI pageUrl = pageUrl(options.url(), page);
                System.out.printf("Fetching page %d/%d: %s%n", page, options.pages(), pageUrl);
                products.addAll(fetchProducts(client, pageUrl));
                if (page < options.pages()) {
                    Thread.sleep(REQUEST_DELAY);
                }
            }

            List<Product> uniqueProducts = uniqueByProductUrl(products);
            writeCsv(uniqueProducts, options.output());
            System.out.printf("Saved %d products to %s%n", uniqueProducts.size(), options.output().toAbsolutePath());
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            printUsage();
            System.exit(2);
        } catch (IOException e) {
            System.err.println("Could not retrieve or write product data: " + e.getMessage());
            System.exit(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Scraping was interrupted.");
            System.exit(1);
        }
    }

    private static List<Product> fetchProducts(HttpClient client, URI url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(url)
                .GET()
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", "Skillcraft-EcommerceScraper/1.0 (educational project)")
                .header("Accept", "text/html,application/xhtml+xml")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("HTTP " + response.statusCode() + " for " + url);
        }

        List<Product> products = ProductHtmlParser.parse(response.body(), url);
        if (products.isEmpty()) {
            System.err.println("Warning: no compatible product cards found on " + url);
        }
        return products;
    }

    private static List<Product> uniqueByProductUrl(List<Product> products) {
        Map<String, Product> unique = new LinkedHashMap<>();
        for (Product product : products) {
            String key = product.productUrl().isBlank()
                    ? product.name() + "|" + product.price() + "|" + product.sourceUrl()
                    : product.productUrl();
            unique.putIfAbsent(key, product);
        }
        return List.copyOf(unique.values());
    }

    private static URI pageUrl(URI catalogUrl, int page) {
        if (page == 1) {
            return catalogUrl;
        }
        String separator = catalogUrl.getQuery() == null ? "?" : "&";
        return URI.create(catalogUrl + separator + "page=" + page);
    }

    private static void writeCsv(List<Product> products, Path output) throws IOException {
        Path parent = output.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            writer.write("name,price,currency,rating,product_url,source_url");
            writer.newLine();
            for (Product product : products) {
                writer.write(String.join(",",
                        csv(product.name()),
                        csv(product.price()),
                        csv(product.currency()),
                        csv(product.rating()),
                        csv(product.productUrl()),
                        csv(product.sourceUrl())));
                writer.newLine();
            }
        }
    }

    private static String csv(String value) {
        String safe = value == null ? "" : value;
        return '"' + safe.replace("\"", "\"\"") + '"';
    }

    private static void printUsage() {
        System.err.println("Usage: EcommerceScraper [--url URL] [--pages NUMBER] [--output FILE]");
    }

    private record Arguments(URI url, int pages, Path output) {
        private static Arguments parse(String[] args) {
            URI url = URI.create(DEFAULT_URL);
            int pages = 1;
            Path output = DEFAULT_OUTPUT;

            for (int i = 0; i < args.length; i++) {
                String option = args[i];
                if ("--help".equals(option) || "-h".equals(option)) {
                    printUsage();
                    System.exit(0);
                }
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("Missing value for " + option);
                }
                String value = args[++i];
                switch (option) {
                    case "--url" -> url = URI.create(value);
                    case "--pages" -> {
                        try {
                            pages = Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("--pages must be a positive whole number");
                        }
                    }
                    case "--output" -> output = Path.of(value);
                    default -> throw new IllegalArgumentException("Unknown option: " + option);
                }
            }

            if (!"http".equalsIgnoreCase(url.getScheme()) && !"https".equalsIgnoreCase(url.getScheme())) {
                throw new IllegalArgumentException("--url must start with http:// or https://");
            }
            if (pages < 1 || pages > 100) {
                throw new IllegalArgumentException("--pages must be between 1 and 100");
            }
            return new Arguments(url, pages, output);
        }
    }
}
