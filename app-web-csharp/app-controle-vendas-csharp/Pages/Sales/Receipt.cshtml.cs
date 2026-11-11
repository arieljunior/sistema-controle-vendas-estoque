using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using Scve.Services;
using Scve.Services.Models;

namespace Scve.Pages.Sales;

public class ReceiptModel(SaleService saleService) : PageModel
{
    public ReceiptView Receipt { get; private set; } = null!;

    public async Task<IActionResult> OnGetAsync(Guid id)
    {
        var receipt = await saleService.GetReceiptAsync(id);
        if (receipt is null)
            return NotFound();

        Receipt = receipt;
        return Page();
    }
}
