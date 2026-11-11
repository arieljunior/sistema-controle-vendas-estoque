using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using System.ComponentModel.DataAnnotations;
using Scve.Domain.Exceptions;
using Scve.Services;
using Scve.Services.Models;

namespace Scve.Pages.Sales;

public class IndexModel(ProductService productService, CartService cartService) : PageModel
{
    public IReadOnlyList<ProductListItem> Products { get; private set; } = [];
    public CartView Cart { get; private set; } = new([], 0);

    [BindProperty]
    public InputModel Input { get; set; } = new();

    public class InputModel
    {
        [Required]
        public Guid ProductId { get; set; }

        [Range(1, 9999, ErrorMessage = "A quantidade deve ser maior que zero.")]
        public int Quantity { get; set; } = 1;
    }

    public Task OnGetAsync() => LoadAsync();

    public async Task<IActionResult> OnPostAddItemAsync()
    {
        if (ModelState.IsValid)
        {
            try
            {
                await cartService.AddItemAsync(Input.ProductId, Input.Quantity);
                return RedirectToPage();
            }
            catch (BusinessException ex)
            {
                ModelState.AddModelError(string.Empty, ex.Message);
            }
        }

        await LoadAsync();
        return Page();
    }

    private async Task LoadAsync()
    {
        Products = await productService.ListAsync();
        Cart = await cartService.GetViewAsync();
    }
}
