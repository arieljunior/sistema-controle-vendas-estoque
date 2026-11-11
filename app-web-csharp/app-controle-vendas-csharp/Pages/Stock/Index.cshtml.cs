using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using Scve.Services;
using Scve.Services.Models;

namespace Scve.Pages.Stock;

public class IndexModel(ProductService productService) : PageModel
{
    [BindProperty(SupportsGet = true)]
    public string? Search { get; set; }

    public IReadOnlyList<ProductListItem> Products { get; private set; } = [];

    public async Task OnGetAsync()
    {
        Products = await productService.ListAsync(Search);
    }
}
