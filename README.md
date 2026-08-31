# SCT_SD_1

Java practice projects.

## Temperature Converter

The existing `Task1.java` and `Task2.java` files contain the original temperature-converter exercises.

## E-commerce Product Scraper

A dependency-free Java program that collects product names, prices, and ratings from the public [Web Scraper Test Sites](https://webscraper.io/test-sites/e-commerce/static/computers/laptops) catalog and writes them to CSV.

The default source is a public test catalog intended for scraping practice. Before changing the URL to a real store, check its terms of service and `robots.txt`, avoid authenticated or personal data, and use a reasonable request rate.

### Requirements

- Java 17 or later

No Maven, Gradle, or third-party JARs are required.

### Run the scraper

```powershell
javac -d out src/main/java/com/skillcraft/scraper/*.java
java -cp out com.skillcraft.scraper.EcommerceScraper
```

The command creates `data/products.csv`.

To scrape more pages or choose another output location:

```powershell
java -cp out com.skillcraft.scraper.EcommerceScraper --pages 3 --output data/laptops.csv
```

To use a compatible catalog URL:

```powershell
java -cp out com.skillcraft.scraper.EcommerceScraper --url "https://webscraper.io/test-sites/e-commerce/static/computers/tablets" --pages 2
```

### CSV columns

| Column | Meaning |
| --- | --- |
| `name` | Product name |
| `price` | Price as displayed by the catalog |
| `currency` | ISO currency code when the page supplies one |
| `rating` | Star rating shown by the catalog |
| `product_url` | Link to the product page |
| `source_url` | Catalog page from which the record was extracted |

### Scraper layout

```text
src/main/java/com/skillcraft/scraper/
  EcommerceScraper.java  # command-line entry point and HTTP requests
  Product.java           # product data model
  ProductHtmlParser.java # standard-library HTML extraction
data/                    # generated CSV files
```
