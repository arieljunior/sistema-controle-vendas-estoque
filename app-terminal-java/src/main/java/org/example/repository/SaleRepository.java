package org.example.repository;

import org.example.model.Sale;

import java.util.ArrayList;
import java.util.List;

public class SaleRepository {
    private final List<Sale> sales = new ArrayList<>();

    public Sale save(Sale sale) {
        sales.add(sale);
        return sale;
    }

    public List<Sale> findAll() {
        return List.copyOf(sales);
    }
}

