using System.Text.Json;
using Scve.Domain.Interfaces;

namespace Scve.Infrastructure.Session;

public class SessionCartStore(IHttpContextAccessor accessor) : ICartStore
{
    private const string Key = "cart";

    private ISession Session => accessor.HttpContext?.Session
        ?? throw new InvalidOperationException("Sessão indisponível fora de uma requisição HTTP.");

    public Task<IReadOnlyList<CartEntry>> GetAsync()
    {
        var json = Session.GetString(Key);
        IReadOnlyList<CartEntry> entries = json is null
            ? []
            : JsonSerializer.Deserialize<List<CartEntry>>(json) ?? [];
        return Task.FromResult(entries);
    }

    public Task SaveAsync(IReadOnlyList<CartEntry> entries)
    {
        Session.SetString(Key, JsonSerializer.Serialize(entries));
        return Task.CompletedTask;
    }

    public Task ClearAsync()
    {
        Session.Remove(Key);
        return Task.CompletedTask;
    }
}
