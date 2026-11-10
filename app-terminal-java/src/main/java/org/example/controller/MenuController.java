package org.example.controller;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class MenuController {
    private static final int SALES = 1;
    private static final int EXIT = 0;

    private final Scanner scanner;
    private final SalesController salesController;

    public MenuController(Scanner scanner, SalesController salesController) {
        this.scanner = scanner;
        this.salesController = salesController;
    }

    public void run() {
        try {
            int choice;
            while ((choice = readChoice()) != EXIT) {
                execute(choice);
            }
        } catch (NoSuchElementException ignored) {
            // input ended: leave the menu loop
        }
        System.out.println("Encerrando o sistema. Até logo!");
    }

    // New menu entries: add a line in readChoice() and a case in execute().
    private int readChoice() {
        System.out.println();
        System.out.println("===== SISTEMA DE CONTROLE DE VENDAS E ESTOQUE =====");
        System.out.println(SALES + " - Registrar Venda");
        System.out.println(EXIT + " - Sair");
        System.out.print("Escolha uma opção: ");
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void execute(int choice) {
        try {
            switch (choice) {
                case SALES -> salesController.run();
                default -> System.out.println("[ERRO] Opção inválida.");
            }
        } catch (NoSuchElementException e) {
            throw e;
        } catch (RuntimeException e) {
            System.out.println("[ERRO] Erro inesperado: " + e.getMessage());
        }
    }
}
