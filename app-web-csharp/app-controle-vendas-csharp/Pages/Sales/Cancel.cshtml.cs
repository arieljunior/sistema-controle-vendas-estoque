using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using Scve.Services;

namespace Scve.Pages.Sales;

public class CancelModel(CartService cartService) : PageModel
{
    [BindProperty(SupportsGet = true)]
    public string? ReturnPage { get; set; }

    // Somente destinos conhecidos (evita open redirect).
    public string BackPage => ReturnPage == "Checkout" ? "/Sales/Checkout" : "/Sales/Index";

    public void OnGet()
    {
    }

    public async Task<IActionResult> OnPostAsync()
    {
        await cartService.ClearAsync();
        TempData["Success"] = "Venda cancelada.";
        return RedirectToPage("/Index");
    }
}
