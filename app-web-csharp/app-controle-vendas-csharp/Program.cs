using System.Globalization;
using Microsoft.AspNetCore.Localization;
using Scve.Domain.Interfaces;
using Scve.Infrastructure.Repositories;
using Scve.Infrastructure.Session;
using Scve.Services;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddRazorPages();
builder.Services.AddDistributedMemoryCache();
builder.Services.AddSession(o =>
{
    o.Cookie.HttpOnly = true;
    o.Cookie.IsEssential = true;
});
builder.Services.AddHttpContextAccessor();
builder.Services.AddSingleton(TimeProvider.System);

// Infrastructure (Singleton: mantém os dados em memória)
builder.Services.AddSingleton<IProductRepository, CsvProductRepository>();
builder.Services.AddSingleton<ISaleRepository, InMemorySaleRepository>();
builder.Services.AddScoped<ICartStore, SessionCartStore>();

// Services
builder.Services.AddScoped<ProductService>();
builder.Services.AddScoped<CartService>();
builder.Services.AddScoped<PaymentService>();
builder.Services.AddScoped<SaleService>();

var app = builder.Build();

if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Error");
    app.UseHsts();
}

var ptBr = new CultureInfo("pt-BR");
var localization = new RequestLocalizationOptions
{
    DefaultRequestCulture = new RequestCulture(ptBr),
    SupportedCultures = [ptBr],
    SupportedUICultures = [ptBr]
};
localization.RequestCultureProviders.Clear();
app.UseRequestLocalization(localization);

app.UseHttpsRedirection();

app.UseRouting();

app.UseSession();

app.UseAuthorization();

app.MapStaticAssets();
app.MapRazorPages()
   .WithStaticAssets();

app.Run();
