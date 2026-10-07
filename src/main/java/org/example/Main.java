package org.example;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String caminhoArquivo = "produtos.csv";

        List<Produto> produtosParaSalvar = new ArrayList<>();
        produtosParaSalvar.add(new Produto(1, "Chocolate", "Massa", 12.50, 50));
        produtosParaSalvar.add(new Produto(2, "Morango", "Picolé", 5.00, 100));
        produtosParaSalvar.add(new Produto(3, "Baunilha", "Massa", 10.00, 30));

        System.out.println("--- ESCREVENDO NO ARQUIVO CSV ---");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(caminhoArquivo))) {
            writer.write("id;nomeSabor;tipo;precoUnitario;quantideEstoque");
            writer.newLine();

            for (Produto p : produtosParaSalvar) {
                writer.write(p.toCSV());
                writer.newLine();
            }
            System.out.println("Arquivo 'produtos.csv' gerado com sucesso!\n");
        } catch (IOException e) {
            System.err.println("Erro ao escrever no arquivo: " + e.getMessage());
        }

        System.out.println("--- LENDO DO ARQUIVO CSV ---");
        try (BufferedReader reader = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha;

            reader.readLine();

            while ((linha = reader.readLine()) != null) {
                if (!linha.trim().isEmpty()) {
                    Produto p = Produto.fromCSV(linha);
                    System.out.println(p);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
        }

    }
}