using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Scve.Infrastructure.Persistence.Migrations
{
    /// <inheritdoc />
    public partial class InitialCreate : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "Produto",
                columns: table => new
                {
                    id = table.Column<Guid>(type: "TEXT", maxLength: 36, nullable: false),
                    nomeSabor = table.Column<string>(type: "TEXT", maxLength: 50, nullable: false),
                    tipo = table.Column<string>(type: "TEXT", maxLength: 10, nullable: false),
                    precoUnitario = table.Column<decimal>(type: "decimal(10,2)", nullable: false),
                    quantidadeEstoque = table.Column<int>(type: "INTEGER", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Produto", x => x.id);
                    table.CheckConstraint("CK_Produto_precoUnitario", "\"precoUnitario\" > 0");
                    table.CheckConstraint("CK_Produto_quantidadeEstoque", "\"quantidadeEstoque\" >= 0");
                });

            migrationBuilder.CreateTable(
                name: "Venda",
                columns: table => new
                {
                    id = table.Column<Guid>(type: "TEXT", maxLength: 36, nullable: false),
                    dataHora = table.Column<DateTime>(type: "TEXT", nullable: false),
                    formaPagamento = table.Column<string>(type: "TEXT", maxLength: 10, nullable: false),
                    valorTotal = table.Column<decimal>(type: "decimal(10,2)", nullable: false),
                    valorPago = table.Column<decimal>(type: "decimal(10,2)", nullable: false),
                    troco = table.Column<decimal>(type: "decimal(10,2)", nullable: true),
                    status = table.Column<string>(type: "TEXT", maxLength: 10, nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Venda", x => x.id);
                });

            migrationBuilder.CreateTable(
                name: "ItemVenda",
                columns: table => new
                {
                    id = table.Column<Guid>(type: "TEXT", maxLength: 36, nullable: false),
                    produtoId = table.Column<Guid>(type: "TEXT", maxLength: 36, nullable: false),
                    quantidade = table.Column<int>(type: "INTEGER", nullable: false),
                    precoUnitarioMomento = table.Column<decimal>(type: "decimal(10,2)", nullable: false),
                    vendaId = table.Column<Guid>(type: "TEXT", maxLength: 36, nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_ItemVenda", x => x.id);
                    table.CheckConstraint("CK_ItemVenda_quantidade", "\"quantidade\" > 0");
                    table.ForeignKey(
                        name: "FK_ItemVenda_Produto_produtoId",
                        column: x => x.produtoId,
                        principalTable: "Produto",
                        principalColumn: "id",
                        onDelete: ReferentialAction.Restrict);
                    table.ForeignKey(
                        name: "FK_ItemVenda_Venda_vendaId",
                        column: x => x.vendaId,
                        principalTable: "Venda",
                        principalColumn: "id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateIndex(
                name: "IX_ItemVenda_produtoId",
                table: "ItemVenda",
                column: "produtoId");

            migrationBuilder.CreateIndex(
                name: "IX_ItemVenda_vendaId",
                table: "ItemVenda",
                column: "vendaId");

            migrationBuilder.CreateIndex(
                name: "IX_Produto_nomeSabor",
                table: "Produto",
                column: "nomeSabor",
                unique: true);

            migrationBuilder.CreateIndex(
                name: "IX_Venda_dataHora",
                table: "Venda",
                column: "dataHora");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "ItemVenda");

            migrationBuilder.DropTable(
                name: "Produto");

            migrationBuilder.DropTable(
                name: "Venda");
        }
    }
}
