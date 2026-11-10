package org.example;

import org.example.controller.MenuController;
import org.example.controller.SalesController;
import org.example.repository.CsvProductRepository;
import org.example.repository.ProductRepository;
import org.example.repository.SaleRepository;
import org.example.service.CartService;
import org.example.service.PaymentService;
import org.example.service.ProductService;
import org.example.service.SaleService;
import org.example.view.SalesView;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

        ProductRepository productRepository = new CsvProductRepository(Path.of("produtos.csv"));
        ProductService productService = new ProductService(productRepository);
        SaleService saleService = new SaleService(productRepository, new SaleRepository(), new PaymentService());
        SalesController salesController = new SalesController(new SalesView(scanner), productService,
                new CartService(productService), saleService);

        new MenuController(scanner, salesController).run();
    }
}
