using Scve.Domain.Entities;
using Scve.Domain.Enums;
using Scve.Domain.Exceptions;
using Scve.Domain.Interfaces;
using Scve.Services.Models;

namespace Scve.Services;

public class SaleService(
    CartService cartService,
    IProductRepository products,
    ISaleRepository sales,
    PaymentService payments,
    TimeProvider time)
{
    // Premissa do MVP: um único usuário; serializa a baixa de estoque.
    private static readonly SemaphoreSlim Gate = new(1, 1);

    public async Task<Guid> CheckoutAsync(PaymentMethod method, decimal? amountPaid)
    {
        await Gate.WaitAsync();
        try
        {
            var cart = await cartService.LoadAsync();
            if (cart.IsEmpty)
                throw new BusinessException("O carrinho está vazio. Adicione ao menos um item.");

            await ValidateStockAsync(cart);

            var sale = Sale.FromCart(cart, method, time.GetLocalNow().DateTime);
            var payment = payments.Process(method, sale.TotalAmount, amountPaid);

            await ApplyStockDeductionAsync(cart);

            sale.Complete(payment.AmountPaid, payment.Change);
            await sales.AddAsync(sale);
            await cartService.ClearAsync();
            return sale.Id;
        }
        finally
        {
            Gate.Release();
        }
    }

    public async Task<ReceiptView?> GetReceiptAsync(Guid id)
    {
        var sale = await sales.GetByIdAsync(id);
        if (sale is null)
            return null;

        var lines = sale.Items
            .Select(i => new LineView(i.FlavorName, i.Quantity, i.UnitPriceAtSale, i.Subtotal))
            .ToList();
        return new ReceiptView(sale.Id, sale.DateTime, sale.PaymentMethod.ToLabel(), sale.Status.ToLabel(),
            lines, sale.TotalAmount, sale.AmountPaid, sale.Change);
    }

    private async Task ValidateStockAsync(Cart cart)
    {
        foreach (var item in cart.Items)
        {
            var current = await products.GetByIdAsync(item.Product.Id)
                ?? throw new BusinessException("Produto não encontrado. Verifique o código digitado.");
            if (!current.HasStockFor(item.Quantity))
                throw new BusinessException(current.InsufficientStockMessage());
        }
    }

    private async Task ApplyStockDeductionAsync(Cart cart)
    {
        foreach (var item in cart.Items)
            item.Product.DecreaseStock(item.Quantity);

        try
        {
            await products.SaveAllAsync();
        }
        catch
        {
            foreach (var item in cart.Items)
                item.Product.IncreaseStock(item.Quantity);
            throw;
        }
    }
}
