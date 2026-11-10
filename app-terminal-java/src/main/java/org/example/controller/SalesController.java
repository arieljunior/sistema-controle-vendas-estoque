package org.example.controller;

import org.example.model.Cart;
import org.example.model.PaymentMethod;
import org.example.model.Product;
import org.example.model.Sale;
import org.example.service.CartService;
import org.example.service.ProductService;
import org.example.service.SaleService;
import org.example.view.SalesView;

import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** UC01 - Registrar Venda no Carrinho. Business rule violations are IllegalArgumentException. */
public class SalesController {
    private final SalesView view;
    private final ProductService productService;
    private final CartService cartService;
    private final SaleService saleService;

    public SalesController(SalesView view, ProductService productService, CartService cartService,
                           SaleService saleService) {
        this.view = view;
        this.productService = productService;
        this.cartService = cartService;
        this.saleService = saleService;
    }

    public void run() {
        Cart cart = new Cart();
        view.showProducts(productService.listProducts());
        if (!fillCart(cart)) {
            return;
        }
        pay(cart).ifPresent(sale -> view.showReceipt(sale, id -> productService.getById(id).getFlavorName()));
    }

    /** Returns false when the sale was canceled. */
    private boolean fillCart(Cart cart) {
        tryAddItem(cart);
        while (true) {
            view.showCart(cart);
            switch (view.askNextAction()) {
                case ADD_ITEM -> {
                    view.showProducts(productService.listProducts());
                    tryAddItem(cart);
                }
                case CHECKOUT -> {
                    if (!cart.isEmpty()) {
                        return true;
                    }
                    view.showError("O carrinho está vazio. Adicione ao menos um item.");
                }
                case CANCEL -> {
                    if (cancelSale(cart)) {
                        return false;
                    }
                }
            }
        }
    }

    private void tryAddItem(Cart cart) {
        try {
            int number = view.askProductNumber();
            List<Product> products = productService.listProducts();
            if (number < 1 || number > products.size()) {
                throw new IllegalArgumentException("Produto não encontrado. Verifique o código digitado.");
            }
            cartService.addItem(cart, products.get(number - 1).getId(), view.askQuantity());
        } catch (IllegalArgumentException e) {
            view.showError(e.getMessage());
        }
    }

    /** Returns empty when the sale was canceled. */
    private Optional<Sale> pay(Cart cart) {
        while (true) {
            Optional<PaymentMethod> method = view.askPaymentMethod();
            BigDecimal amountPaid = null;
            if (method.isPresent() && method.get() == PaymentMethod.CASH) {
                Optional<BigDecimal> cash = view.askCashAmount();
                if (cash.isPresent()) {
                    amountPaid = cash.get();
                } else {
                    method = Optional.empty();
                }
            }
            if (method.isEmpty()) {
                if (cancelSale(cart)) {
                    return Optional.empty();
                }
                continue;
            }
            try {
                return Optional.of(saleService.checkout(cart, method.get(), amountPaid));
            } catch (IllegalArgumentException | UncheckedIOException e) {
                view.showError(e.getMessage());
            }
        }
    }

    private boolean cancelSale(Cart cart) {
        if (!view.confirmCancellation()) {
            return false;
        }
        cart.clear();
        view.showMessage("Venda cancelada.");
        return true;
    }
}
