using System.Globalization;
using Scve.Domain.Entities;
using Scve.Domain.Enums;
using Scve.Domain.Interfaces;

namespace Scve.Infrastructure.Repositories;

public class CsvProductRepository : IProductRepository
{
    private const string DefaultHeader = "id;nomeSabor;tipo;precoUnitario;quantidadeEstoque";
    private const char Separator = ';';
    private const int Columns = 5;

    private readonly string _path;
    private readonly SemaphoreSlim _writeLock = new(1, 1);
    private readonly List<Product> _products;
    private string _header = DefaultHeader;

    public CsvProductRepository(IConfiguration configuration, IHostEnvironment environment)
    {
        var configured = configuration["Data:ProductsCsvPath"]
            ?? throw new InvalidOperationException("Configuração 'Data:ProductsCsvPath' não encontrada.");
        _path = Path.Combine(environment.ContentRootPath, configured);
        _products = Load();
    }

    public Task<IReadOnlyList<Product>> ListAsync() =>
        Task.FromResult<IReadOnlyList<Product>>(_products.ToList());

    public Task<Product?> GetByIdAsync(Guid id) =>
        Task.FromResult(_products.FirstOrDefault(p => p.Id == id));

    public async Task SaveAllAsync()
    {
        await _writeLock.WaitAsync();
        try
        {
            var lines = new List<string> { _header };
            lines.AddRange(_products.Select(ToLine));

            var temp = _path + ".tmp";
            await File.WriteAllLinesAsync(temp, lines, new System.Text.UTF8Encoding(false));
            File.Move(temp, _path, overwrite: true);
        }
        finally
        {
            _writeLock.Release();
        }
    }

    private List<Product> Load()
    {
        var lines = File.ReadAllLines(_path, System.Text.Encoding.UTF8);
        if (lines.Length > 0)
            _header = lines[0];

        var products = new List<Product>();
        for (var i = 1; i < lines.Length; i++)
        {
            if (!string.IsNullOrWhiteSpace(lines[i]))
                products.Add(Parse(lines[i], i + 1));
        }
        return products;
    }

    private Product Parse(string line, int lineNumber)
    {
        var parts = line.Split(Separator);
        if (parts.Length != Columns)
            throw new InvalidDataException($"Linha {lineNumber} inválida em {_path}.");

        try
        {
            return new Product(
                Guid.Parse(parts[0].Trim()),
                parts[1],
                EnumExtensions.ParseProductType(parts[2]),
                decimal.Parse(parts[3].Trim(), NumberStyles.AllowDecimalPoint, CultureInfo.InvariantCulture),
                int.Parse(parts[4].Trim(), NumberStyles.None, CultureInfo.InvariantCulture));
        }
        catch (Exception e) when (e is ArgumentException or FormatException or OverflowException)
        {
            throw new InvalidDataException($"Linha {lineNumber} inválida em {_path}: {e.Message}", e);
        }
    }

    private static string ToLine(Product product) => string.Join(Separator,
        product.Id,
        product.FlavorName,
        product.Type.ToLabel(),
        product.UnitPrice.ToString("F2", CultureInfo.InvariantCulture),
        product.StockQuantity.ToString(CultureInfo.InvariantCulture));
}
