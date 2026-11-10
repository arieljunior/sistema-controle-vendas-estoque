package org.example.repository;

import org.example.model.Product;
import org.example.model.ProductType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CsvProductRepository implements ProductRepository {
    private static final String HEADER = "id;nomeSabor;tipo;precoUnitario;quantidadeEstoque";
    private static final String SEPARATOR = ";";
    private static final int COLUMNS = 5;

    private final Path file;
    private List<Product> products;

    public CsvProductRepository(Path file) {
        this.file = file;
        this.products = load();
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(products);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return products.stream().filter(product -> product.getId().equals(id)).findFirst();
    }

    @Override
    public void saveAll(List<Product> productsToSave) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        productsToSave.forEach(product -> lines.add(toLine(product)));
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.write(temp, lines, StandardCharsets.UTF_8);
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gravar o arquivo " + file + ".", e);
        }
        this.products = new ArrayList<>(productsToSave);
    }

    private List<Product> load() {
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler o arquivo " + file + ".", e);
        }
        List<Product> loaded = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            if (!lines.get(i).isBlank()) {
                loaded.add(parse(lines.get(i), i + 1));
            }
        }
        return loaded;
    }

    private Product parse(String line, int lineNumber) {
        String[] parts = line.split(SEPARATOR, -1);
        if (parts.length != COLUMNS) {
            throw new IllegalStateException("Linha " + lineNumber + " inválida em " + file + ".");
        }
        try {
            return new Product(
                    UUID.fromString(parts[0].trim()),
                    parts[1],
                    ProductType.fromLabel(parts[2]),
                    new BigDecimal(parts[3].trim()),
                    Integer.parseInt(parts[4].trim()));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Linha " + lineNumber + " inválida em " + file + ": " + e.getMessage(), e);
        }
    }

    private String toLine(Product product) {
        return String.join(SEPARATOR,
                product.getId().toString(),
                product.getFlavorName(),
                product.getType().getLabel(),
                product.getUnitPrice().toPlainString(),
                String.valueOf(product.getStockQuantity()));
    }
}
