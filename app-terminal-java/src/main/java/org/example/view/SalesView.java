package org.example.view;

import org.example.model.Cart;
import org.example.model.CartItem;
import org.example.model.PaymentMethod;
import org.example.model.Product;
import org.example.model.Sale;
import org.example.model.SaleItem;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;
import java.util.function.Function;

public class SalesView {
    public enum NextAction { ADD_ITEM, CHECKOUT, CANCEL }

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final Scanner scanner;

    public SalesView(Scanner scanner) {
        this.scanner = scanner;
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    public void showError(String message) {
        System.out.println("[ERRO] " + message);
    }

    public void showProducts(List<Product> products) {
        System.out.println();
        System.out.println("--- PRODUTOS DISPONÍVEIS ---");
        System.out.println(String.format("%-4s %-20s %-8s %10s %8s", "Nº", "Sabor", "Tipo", "Preço", "Estoque"));
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            System.out.println(String.format("%-4d %-20s %-8s %10s %8d%s", i + 1, p.getFlavorName(),
                    p.getType().getLabel(), "R$ " + p.getUnitPrice().toPlainString(), p.getStockQuantity(),
                    p.isLowStock() ? "  << ESTOQUE BAIXO" : ""));
        }
    }

    public void showCart(Cart cart) {
        System.out.println();
        System.out.println("--- CARRINHO ---");
        if (cart.isEmpty()) {
            System.out.println("(vazio)");
        }
        for (CartItem item : cart.getItems()) {
            System.out.println(String.format("%-20s x%-3d R$ %s", item.getProduct().getFlavorName(),
                    item.getQuantity(), item.subtotal().toPlainString()));
        }
        System.out.println("Subtotal: R$ " + cart.total().toPlainString());
    }

    public int askProductNumber() {
        return readInt("Informe o número do produto: ");
    }

    public int askQuantity() {
        return readInt("Informe a quantidade: ");
    }

    public NextAction askNextAction() {
        System.out.println();
        System.out.println("1 - Adicionar mais itens");
        System.out.println("2 - Finalizar e pagar");
        System.out.println("3 - Cancelar venda");
        while (true) {
            switch (readInt("Opção: ")) {
                case 1 -> { return NextAction.ADD_ITEM; }
                case 2 -> { return NextAction.CHECKOUT; }
                case 3 -> { return NextAction.CANCEL; }
                default -> showError("Opção inválida.");
            }
        }
    }

    /** Returns empty when the user chooses to cancel the sale. */
    public Optional<PaymentMethod> askPaymentMethod() {
        System.out.println();
        System.out.println("--- FORMA DE PAGAMENTO ---");
        System.out.println("1 - Dinheiro");
        System.out.println("2 - Pix");
        System.out.println("0 - Cancelar venda");
        while (true) {
            switch (readInt("Opção: ")) {
                case 1 -> { return Optional.of(PaymentMethod.CASH); }
                case 2 -> { return Optional.of(PaymentMethod.PIX); }
                case 0 -> { return Optional.empty(); }
                default -> showError("Opção inválida.");
            }
        }
    }

    /** Returns empty when the user types 'C' to cancel the sale. */
    public Optional<BigDecimal> askCashAmount() {
        while (true) {
            String input = readLine("Valor pago em dinheiro (ou 'C' para cancelar a venda): R$ ");
            if (input.equalsIgnoreCase("C")) {
                return Optional.empty();
            }
            try {
                BigDecimal amount = new BigDecimal(input.replace(',', '.'));
                if (amount.signum() > 0) {
                    return Optional.of(amount);
                }
            } catch (NumberFormatException ignored) {
                // falls through to the error message
            }
            showError("Informe um valor numérico maior que zero.");
        }
    }

    public boolean confirmCancellation() {
        while (true) {
            String answer = readLine("Confirma o cancelamento da venda? (S/N): ");
            if (answer.equalsIgnoreCase("S")) {
                return true;
            }
            if (answer.equalsIgnoreCase("N")) {
                return false;
            }
            showError("Responda S ou N.");
        }
    }

    public void showReceipt(Sale sale, Function<UUID, String> productNameResolver) {
        System.out.println();
        System.out.println("===== RECIBO DA VENDA =====");
        System.out.println("Venda: " + sale.getId());
        System.out.println("Data/Hora: " + sale.getDateTime().format(DATE_FORMAT));
        for (SaleItem item : sale.getItems()) {
            System.out.println(String.format("%-20s x%-3d R$ %s", productNameResolver.apply(item.getProductId()),
                    item.getQuantity(), item.subtotal().toPlainString()));
        }
        System.out.println("Total: R$ " + sale.getTotalAmount().toPlainString());
        System.out.println("Pagamento: " + sale.getPaymentMethod().getLabel());
        System.out.println("Valor pago: R$ " + sale.getAmountPaid().toPlainString());
        if (sale.getChange() != null) {
            System.out.println("Troco: R$ " + sale.getChange().toPlainString());
        }
        System.out.println("Status: " + sale.getStatus().getCode());
        System.out.println("Venda registrada com sucesso!");
    }

    // Scanner.nextLine() throws NoSuchElementException when the input ends
    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt));
            } catch (NumberFormatException e) {
                showError("Digite um número inteiro válido.");
            }
        }
    }
}
