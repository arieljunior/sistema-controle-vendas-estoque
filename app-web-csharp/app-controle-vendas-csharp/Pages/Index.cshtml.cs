using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using Scve.Services;

namespace Scve.Pages;

public class IndexModel(CartService cartService) : PageModel
{
    public void OnGet()
    {
    }

    public async Task<IActionResult> OnPostStartSaleAsync()
    {
        await cartService.ClearAsync();
        return RedirectToPage("/Sales/Index");
    }
}
