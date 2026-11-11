using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using System.ComponentModel.DataAnnotations;
using Scve.Domain.Enums;
using Scve.Domain.Exceptions;
using Scve.Services;
using Scve.Services.Models;

namespace Scve.Pages.Sales;

public class CheckoutModel(CartService cartService, SaleService saleService) : PageModel
{
    public CartView Cart { get; private set; } = new([], 0);

    [BindProperty]
    public InputModel Input { get; set; } = new();

    public class InputModel
    {
        [Required(ErrorMessage = "Selecione a forma de pagamento.")]
        public PaymentMethod? Method { get; set; }

        public string? AmountPaid { get; set; }
    }

    public async Task<IActionResult> OnGetAsync()
    {
        await LoadCartAsync();
        return Cart.IsEmpty ? EmptyCartRedirect() : Page();
    }

    public async Task<IActionResult> OnPostAsync()
    {
        await LoadCartAsync();
        if (Cart.IsEmpty)
            return EmptyCartRedirect();

        if (!ModelState.IsValid)
            return Page();

        decimal? amountPaid = null;
        if (Input.Method == PaymentMethod.Cash)
        {
            if (!BrazilianMoney.TryParse(Input.AmountPaid, out var parsed))
            {
                ModelState.AddModelError("Input.AmountPaid", "Valor inválido. Informe o valor no formato 0,00.");
                return Page();
            }
            amountPaid = parsed;
        }

        try
        {
            var saleId = await saleService.CheckoutAsync(Input.Method!.Value, amountPaid);
            return RedirectToPage("Receipt", new { id = saleId });
        }
        catch (BusinessException ex)
        {
            ModelState.AddModelError(string.Empty, ex.Message);
            await LoadCartAsync();
            return Page();
        }
    }

    private async Task LoadCartAsync() => Cart = await cartService.GetViewAsync();

    private IActionResult EmptyCartRedirect()
    {
        TempData["Error"] = "O carrinho está vazio. Adicione ao menos um item.";
        return RedirectToPage("Index");
    }
}
