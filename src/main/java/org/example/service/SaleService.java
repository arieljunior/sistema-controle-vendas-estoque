package org.example.service;

import org.example.model.Cart;
import org.example.model.CartItem;
import org.example.model.PaymentMethod;
import org.example.model.Product;
import org.example.model.Sale;
import org.example.repository.ProductRepository;
import org.example.repository.SaleRepository;

import java.io.UncheckedIOException;
import java.math.BigDecimal;

public class SaleService {
    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final PaymentService paymentService;

    public SaleService(ProductRepository productRepository, SaleRepository saleRepository,
                       PaymentService paymentService) {
        this.productRepository = productRepository;
        this.saleRepository = saleRepository;
        this.paymentService = paymentService;
    }

    /** amountPaid is only required for CASH payments. */
    public Sale checkout(Cart cart, PaymentMethod method, BigDecimal amountPaid) {
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("O carrinho está vazio. Adicione ao menos um item.");
        }
        cart.getItems().forEach(this::validateStock);

        Sale sale = Sale.fromCart(cart, method);
        PaymentService.PaymentResult payment = paymentService.process(method, sale.getTotalAmount(), amountPaid);

        applyStockDeduction(cart);
        sale.complete(payment.amountPaid(), payment.change());
        return saleRepository.save(sale);
    }

    private void validateStock(CartItem item) {
        Product current = productRepository.findById(item.getProduct().getId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado. Verifique o código digitado."));
        if (!current.hasStockFor(item.getQuantity())) {
            throw new IllegalArgumentException("Estoque insuficiente para o produto " + current.getFlavorName()
                    + ". Quantidade disponível em estoque: " + current.getStockQuantity() + ".");
        }
    }

    private void applyStockDeduction(Cart cart) {
        cart.getItems().forEach(item -> item.getProduct().decreaseStock(item.getQuantity()));
        try {
            productRepository.saveAll(productRepository.findAll());
        } catch (UncheckedIOException e) {
            cart.getItems().forEach(item -> item.getProduct().increaseStock(item.getQuantity()));
            throw e;
        }
    }
}
