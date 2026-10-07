package org.example;

public class Produto {
    private int id;
    private String nomeSabor;
    private String tipo;
    private double precoUnitario;
    private int quantideEstoque;

    public Produto(int id, String nomeSabor, String tipo, double precoUnitario, int quantideEstoque) {
        this.id = id;
        this.nomeSabor = nomeSabor;
        this.tipo = tipo;
        this.precoUnitario = precoUnitario;
        this.quantideEstoque = quantideEstoque;
    }

    public String toCSV() {
        return id + ";" + nomeSabor + ";" + tipo + ";" + precoUnitario + ";" + quantideEstoque;
    }

    public static Produto fromCSV(String linha) {
        String[] partes = linha.split(";");
        int id = Integer.parseInt(partes[0]);
        String nomeSabor = partes[1];
        String tipo = partes[2];
        double precoUnitario = Double.parseDouble(partes[3]);
        int quantideEstoque = Integer.parseInt(partes[4]);

        return new Produto(id, nomeSabor, tipo, precoUnitario, quantideEstoque);
    }

    @Override
    public String toString() {
        return "ID: " + id + " | Produto: " + nomeSabor + " | Tipo: " + tipo +
                " | Preço: R$ " + precoUnitario + " | Estoque: " + quantideEstoque;
    }
}
