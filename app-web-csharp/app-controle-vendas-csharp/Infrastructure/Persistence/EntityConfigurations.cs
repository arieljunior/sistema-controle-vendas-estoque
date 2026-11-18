using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Microsoft.EntityFrameworkCore.Storage.ValueConversion;
using Scve.Domain.Entities;
using Scve.Domain.Enums;

namespace Scve.Infrastructure.Persistence;

internal static class EnumConverters
{
    public static readonly ValueConverter<ProductType, string> ProductType = new(
        v => v == Domain.Enums.ProductType.Picole ? "PICOLE" : "MASSA",
        v => v == "PICOLE" ? Domain.Enums.ProductType.Picole : Domain.Enums.ProductType.Massa);

    public static readonly ValueConverter<PaymentMethod, string> PaymentMethod = new(
        v => v == Domain.Enums.PaymentMethod.Cash ? "DINHEIRO" : "PIX",
        v => v == "DINHEIRO" ? Domain.Enums.PaymentMethod.Cash : Domain.Enums.PaymentMethod.Pix);

    public static readonly ValueConverter<SaleStatus, string> SaleStatus = new(
        v => v == Domain.Enums.SaleStatus.Pending ? "PENDENTE"
            : v == Domain.Enums.SaleStatus.Completed ? "CONCLUIDA" : "CANCELADA",
        v => v == "PENDENTE" ? Domain.Enums.SaleStatus.Pending
            : v == "CONCLUIDA" ? Domain.Enums.SaleStatus.Completed : Domain.Enums.SaleStatus.Canceled);
}

internal class ProductConfiguration : IEntityTypeConfiguration<Product>
{
    public void Configure(EntityTypeBuilder<Product> builder)
    {
        builder.ToTable("Produto", t =>
        {
            t.HasCheckConstraint("CK_Produto_precoUnitario", "\"precoUnitario\" > 0");
            t.HasCheckConstraint("CK_Produto_quantidadeEstoque", "\"quantidadeEstoque\" >= 0");
        });

        builder.HasKey(p => p.Id);
        builder.Property(p => p.Id).HasColumnName("id").HasMaxLength(36).ValueGeneratedNever();
        builder.Property(p => p.FlavorName).HasColumnName("nomeSabor").HasMaxLength(50).IsRequired();
        builder.Property(p => p.Type).HasColumnName("tipo").HasMaxLength(10)
            .HasConversion(EnumConverters.ProductType).IsRequired();
        builder.Property(p => p.UnitPrice).HasColumnName("precoUnitario").HasColumnType("decimal(10,2)").IsRequired();
        builder.Property(p => p.StockQuantity).HasColumnName("quantidadeEstoque").IsRequired();

        builder.HasIndex(p => p.FlavorName).IsUnique();
    }
}

internal class SaleConfiguration : IEntityTypeConfiguration<Sale>
{
    public void Configure(EntityTypeBuilder<Sale> builder)
    {
        builder.ToTable("Venda");

        builder.HasKey(s => s.Id);
        builder.Property(s => s.Id).HasColumnName("id").HasMaxLength(36).ValueGeneratedNever();
        builder.Property(s => s.DateTime).HasColumnName("dataHora").IsRequired();
        builder.Property(s => s.PaymentMethod).HasColumnName("formaPagamento").HasMaxLength(10)
            .HasConversion(EnumConverters.PaymentMethod).IsRequired();
        builder.Property(s => s.TotalAmount).HasColumnName("valorTotal").HasColumnType("decimal(10,2)").IsRequired();
        builder.Property(s => s.AmountPaid).HasColumnName("valorPago").HasColumnType("decimal(10,2)").IsRequired();
        builder.Property(s => s.Change).HasColumnName("troco").HasColumnType("decimal(10,2)");
        builder.Property(s => s.Status).HasColumnName("status").HasMaxLength(10)
            .HasConversion(EnumConverters.SaleStatus).IsRequired();

        builder.HasIndex(s => s.DateTime);

        builder.Navigation(s => s.Items).HasField("_items").UsePropertyAccessMode(PropertyAccessMode.Field);
    }
}

internal class SaleItemConfiguration : IEntityTypeConfiguration<SaleItem>
{
    private const string SaleIdColumn = "SaleId";

    public void Configure(EntityTypeBuilder<SaleItem> builder)
    {
        builder.ToTable("ItemVenda", t => t.HasCheckConstraint("CK_ItemVenda_quantidade", "\"quantidade\" > 0"));

        builder.HasKey(i => i.Id);
        builder.Property(i => i.Id).HasColumnName("id").HasMaxLength(36).ValueGeneratedNever();
        builder.Property<Guid>(SaleIdColumn).HasColumnName("vendaId").HasMaxLength(36).IsRequired();
        builder.Property(i => i.ProductId).HasColumnName("produtoId").HasMaxLength(36).IsRequired();
        builder.Property(i => i.Quantity).HasColumnName("quantidade").IsRequired();
        builder.Property(i => i.UnitPriceAtSale).HasColumnName("precoUnitarioMomento")
            .HasColumnType("decimal(10,2)").IsRequired();

        builder.HasOne<Sale>()
            .WithMany(s => s.Items)
            .HasForeignKey(SaleIdColumn)
            .OnDelete(DeleteBehavior.Cascade);

        builder.HasOne(i => i.Product)
            .WithMany()
            .HasForeignKey(i => i.ProductId)
            .OnDelete(DeleteBehavior.Restrict);
    }
}
