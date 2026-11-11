---
goal: Implementar na versão web (ASP.NET Core Razor Pages, C#) o UC01 "Registrar Venda no Carrinho" com pagamento em Dinheiro ou Pix (mock), lendo produtos de CSV, sem banco de dados e sem testes, com interface escura simples
version: 2.0
date_created: 2026-11-10
last_updated: 2026-11-10
owner: Equipe SCVE
status: 'Planned'
tags: [feature, architecture, csharp, razor-pages, csv, mock, mvp]
---

# Introdução

![Status: Planned](https://img.shields.io/badge/status-Planned-blue)

Plano de migração do fluxo UC01 já construído em `app-terminal-java` (ver `plan/feature-cart-sale-checkout-1.md` e `app-terminal-java/docs/onboarding.md`) para `app-web-csharp`, conforme `docs/spec.md` e os diagramas de sequência em `docs/`. A arquitetura segue `app-web-csharp/docs/guia_dev.md`. Escopo: **um único fluxo** (FP com Pix, FA01 Dinheiro, FA04 Cancelamento, FE01–FE03). **Sem banco de dados e sem testes automatizados**: produtos vêm de um CSV, vendas ficam em memória e o Pix é mockado. Código em inglês; mensagens ao usuário em pt-BR.

## 1. Requirements & Constraints

- **REQ-001**: Cobrir RF02 (listagem), RF03/RN06 (estoque baixo), RF04, RF05, RF06, RF09 e RN01–RN05.
- **REQ-002**: Fluxo web: tela inicial com botão **"Realizar Venda"** → listar produtos → adicionar itens (repetível) → finalizar → escolher pagamento → recibo. Cancelamento (FA04) com confirmação a qualquer momento antes do pagamento.
- **REQ-003**: Pix mockado dentro do `PaymentService`: aprova sempre e registra log `PIX gateway BYPASS enabled...` (igual ao Java). FE04 fica fora do escopo enquanto não houver gateway real.
- **REQ-004 (Dinheiro em formato brasileiro)**: o valor pago é digitado em `R$` pt-BR. Aceita `50`, `50,5`, `50,00`, `1.250,50` e `R$ 1.250,50`; rejeita `50.00` (ponto não é decimal), negativos e mais de 2 casas, com a mensagem "Valor inválido. Informe o valor no formato 0,00.". Valores exibidos como `R$ 1.250,50`. Validação `valorPago >= total` (RN04/FE03) e troco = pago − total (RN05).
- **REQ-005**: Baixa de estoque só após o pagamento aprovado (RN03), gravada no CSV; se a gravação falhar, o estoque em memória é restaurado e a venda não é registrada.
- **REQ-006**: Mensagens de erro idênticas às da spec (FE01–FE03) e às da versão Java.
- **REQ-007 (UI)**: interface simples, **tema escuro**, um único `site.css` próprio (sem Bootstrap/jQuery), responsiva (uso em celular). Tela inicial com um botão de destaque "Realizar Venda" que inicia um novo fluxo (carrinho zerado).
- **REQ-008**: Rastreabilidade com os diagramas: cada mensagem mapeada para um método (seção 2.1).
- **CON-001**: .NET 10 (`net10.0`), Razor Pages, nullable ativo, projeto único.
- **CON-002**: Camadas e dependências conforme guia: `Pages → Services → Domain ← Infrastructure`. `PageModel` nunca acessa Repository.
- **CON-003**: Dados: cópia própria do `produtos.csv` em `Data/produtos.csv` (mesmo formato e UUIDs do Java; `;`, UTF-8, ponto decimal). Caminho em `appsettings.json` (`Data:ProductsCsvPath`). Vendas apenas em memória (somem ao reiniciar). **Sem banco, sem EF Core.**
- **CON-004**: Valores monetários `decimal` com 2 casas, `MidpointRounding.AwayFromZero` (equivale ao `HALF_UP` do Java). CSV lido/gravado com `CultureInfo.InvariantCulture`.
- **CON-005**: Estado do carrinho em `ISession` (somente `ProductId` + `Quantity`); preços e estoque sempre relidos do repositório.
- **CON-006**: Cultura `pt-BR` fixa (`RequestLocalization`) para exibição de moeda e datas.
- **CON-007 (Simplicidade)**: Sem testes automatizados, MediatR, AutoMapper, CQRS, Strategy, Unit of Work, múltiplos projetos ou autenticação. Interfaces apenas nas fronteiras de I/O: `IProductRepository`, `ISaleRepository`, `ICartStore`. `PaymentService` é uma classe concreta com `switch` (como no Java).
- **CON-008**: Fora de escopo (igual ao Java): FA02, FA03, UC02–UC04, autenticação/perfis.
- **GUD-001**: Nomes de classes/métodos em inglês; sufixo `Async` em todo I/O.
- **GUD-002**: Invariantes no domínio (setters privados; criação por construtor/método de fábrica).
- **PAT-001**: Repository (CSV/memória). **PAT-002**: PRG nas páginas. **PAT-003**: `BusinessException` → `ModelState`.

## 2. Implementation Steps

Namespace raiz `Scve`; pasta `app-web-csharp/app-controle-vendas-csharp/`.

```text
Domain/{Entities,Enums,Exceptions,Interfaces}
Infrastructure/{Repositories,Session}
Services/{Models}
Data/produtos.csv
Pages/{Index, Sales/{Index,Checkout,Cancel,Receipt}, Shared}
wwwroot/css/site.css
```

### 2.1 Mapeamento diagrama → código

| Mensagem do diagrama | Implementação |
| --- | --- |
| `consultar produtos disponíveis` | `ProductService.ListAsync()` → `Pages/Sales/Index` |
| `adicionarItem(código, quantidade)` + `verificarEstoque` [RN01] | `CartService.AddItemAsync` → `Cart.AddItem` |
| `calcularSubtotal()` | `CartItem.Subtotal` / `Cart.Total` |
| `solicitarFinalizacao(formaPagamento, valorPago)` | `SaleService.CheckoutAsync(CheckoutRequest)` |
| `validarValorPago` [RN04] / `calcularTroco` [RN05] | `PaymentService.PayWithCash` |
| `processarCobrancaPix` [RN02] | `PaymentService.PayWithPix` (bypass mockado) |
| `darBaixaEstoque` [RN03] | `Product.DecreaseStock` + `IProductRepository.SaveAllAsync` (em `SaleService`) |
| `atualizarStatus(CONCLUIDO)` | `Sale.Complete(amountPaid, change)` |
| `exibirResumoVenda` | `Pages/Sales/Receipt` |

### Implementation Phase 1 — Fundação

- GOAL-001: Projeto, dados e visual base prontos.

| Task | Description | Completed | Date |
| --- | --- | --- | --- |
| TASK-001 | `.csproj`: `RootNamespace` → `Scve`; incluir `Data\produtos.csv` na publicação (`CopyToPublishDirectory`). Criar as pastas da estrutura acima. | | |
| TASK-002 | Copiar `app-terminal-java/produtos.csv` para `Data/produtos.csv` (mesmos 3 UUIDs; UTF-8; `Picolé` correto). `appsettings.json`: `Data:ProductsCsvPath = "Data/produtos.csv"`. | | |
| TASK-003 | `Program.cs` (base): `AddRazorPages`, `AddDistributedMemoryCache` + `AddSession` (cookie `HttpOnly`, essencial), `AddHttpContextAccessor`, `AddSingleton(TimeProvider.System)`, `RequestLocalizationOptions` `pt-BR`, `UseSession` após `UseRouting`. | | |
| TASK-004 | Visual: remover Bootstrap/jQuery do `_Layout` e apagar `wwwroot/lib`, `_Layout.cshtml.css`, `_ValidationScriptsPartial` e a página `Privacy`. Criar `wwwroot/css/site.css` (tema escuro com variáveis CSS; classes do guia, seção F). `_Layout`: cabeçalho "SCVE" simples e `_Alerts.cshtml` (exibe `TempData["Success"]` e erros). | | |

### Implementation Phase 2 — Domain

- GOAL-002: Regras e invariantes sem dependência de framework.

| Task | Description | Completed | Date |
| --- | --- | --- | --- |
| TASK-005 | `Domain/Enums`: `ProductType` (`Picole`, `Massa`), `PaymentMethod` (`Cash`, `Pix`), `SaleStatus` (`Pending`, `Completed`, `Canceled`) + extensão `ToLabel()` com o texto pt-BR para a UI (`Picolé`, `Massa`, `Dinheiro`, `Pix`, `Concluída`...). | | |
| TASK-006 | `Domain/Exceptions/BusinessException` (única exceção de negócio; mensagem pt-BR da spec: FE01 "Produto não encontrado. Verifique o código digitado.", FE02 "Estoque insuficiente para o produto X. Quantidade disponível em estoque: N.", FE03 "Valor informado é insuficiente para cobrir o total da venda. Faltam: R$ X.", carrinho vazio, quantidade inválida). | | |
| TASK-007 | `Product` (`Guid Id`, `FlavorName` ≤ 50, `ProductType Type`, `UnitPrice` > 0, `StockQuantity` ≥ 0; sem setters públicos; construtor valida): `IsLowStock` (≤ 5), `HasStockFor(int)`, `DecreaseStock(int)`, `IncreaseStock(int)` (restaura em falha de gravação). | | |
| TASK-008 | `Cart` e `CartItem` (transitórios): `AddItem(Product, int)` valida quantidade > 0 e RN01 somando o que já está no carrinho; `Total`, `IsEmpty`, `Clear()`, `Items` somente leitura. | | |
| TASK-009 | `Sale` (`Id`, `DateTime`, `PaymentMethod`, `TotalAmount`, `AmountPaid`, `Change?`, `Status`, `Items`) e `SaleItem` (`ProductId`, `FlavorName`, `Quantity` > 0, `UnitPriceAtSale`, `Subtotal`). `Sale.FromCart(cart, method, now)` cria `Pending`; `Complete(amountPaid, change)` ⇒ `Completed`. `now` vem de `TimeProvider.GetLocalNow()` injetado no `SaleService`. | | |
| TASK-010 | `Domain/Interfaces`: `IProductRepository` (`ListAsync`, `GetByIdAsync`, `SaveAllAsync`), `ISaleRepository` (`AddAsync`, `GetByIdAsync`), `ICartStore` (`GetAsync`/`SaveAsync`/`ClearAsync` de `CartState` = lista de `{ProductId, Quantity}`). | | |

### Implementation Phase 3 — Infrastructure

- GOAL-003: Dados em CSV/memória e carrinho em sessão.

| Task | Description | Completed | Date |
| --- | --- | --- | --- |
| TASK-011 | `Repositories/CsvProductRepository` (Singleton): lê `Data/produtos.csv` uma vez (UTF-8, `;`, cabeçalho ignorado, colunas por posição, `InvariantCulture`); linha inválida ⇒ `InvalidDataException` com o número da linha; `SaveAllAsync` regrava com o mesmo cabeçalho de forma atômica (arquivo temporário + `File.Move`), com `SemaphoreSlim` na escrita. | | |
| TASK-012 | `Repositories/InMemorySaleRepository` (Singleton): lista protegida por `lock`; `AddAsync`, `GetByIdAsync`. | | |
| TASK-013 | `Session/SessionCartStore : ICartStore` (via `IHttpContextAccessor`; JSON do `CartState` na `ISession`). | | |

### Implementation Phase 4 — Services

- GOAL-004: Casos de uso e regras de negócio.

| Task | Description | Completed | Date |
| --- | --- | --- | --- |
| TASK-014 | `Services/BrazilianMoney` (estática): `TryParse(string, out decimal)` estrito — remove `R$` e espaços; aceita inteiro, vírgula com 1–2 casas e milhar com ponto em grupos de 3 (`1.250,50`); rejeita `50.00`, negativo, zero, texto e >2 casas. `Format(decimal)` ⇒ `ToString("C", pt-BR)`. | | |
| TASK-015 | `Services/Models`: records de leitura — `ProductListItem`, `CartView`/`CartLineView`, `ReceiptView`. As páginas não manipulam entidades. | | |
| TASK-016 | `ProductService.ListAsync()` (ordem do CSV, com `IsLowStock`). | | |
| TASK-017 | `CartService`: `GetAsync()`, `AddItemAsync(Guid productId, int quantity)` (FE01 se inexistente; RN01/FE02 via `Cart.AddItem`), `ClearAsync()` (FA04). Reidrata o `Cart` a partir de `ICartStore` + `IProductRepository`. | | |
| TASK-018 | `PaymentService.Process(PaymentMethod, decimal total, decimal? amountPaid)` com `switch`: `PayWithCash` (RN04 ⇒ FE03; troco RN05) e `PayWithPix` (RN02; bypass mockado: `ILogger` warning `PIX gateway BYPASS enabled...`, `AmountPaid = total`, `Change = null`; comentário indica o ponto de troca pelo gateway real). Retorna `PaymentResult(amountPaid, change)`. | | |
| TASK-019 | `SaleService.CheckoutAsync(PaymentMethod, decimal? amountPaid)` sob `SemaphoreSlim`: carrinho vazio ⇒ erro; revalida estoque com dados atuais; cria `Sale` (Pending); `PaymentService.Process`; **só em sucesso**: `DecreaseStock` dos itens → `SaveAllAsync` (em falha: `IncreaseStock` para restaurar e propagar o erro) → `Sale.Complete` → `AddAsync` → limpa o carrinho. Retorna o `Guid` da venda. | | |
| TASK-020 | `SaleService.GetReceiptAsync(Guid id)` ⇒ `ReceiptView` (itens, total, pago, troco, forma, data/hora); id inexistente ⇒ `null`. | | |

### Implementation Phase 5 — Pages

- GOAL-005: UI Razor Pages escura e simples, seguindo PRG e sem regra de negócio.

| Task | Description | Completed | Date |
| --- | --- | --- | --- |
| TASK-021 | `Pages/Index` (início): título, breve descrição e **botão grande "Realizar Venda"** (`<form method="post">`, `OnPostStartSaleAsync` ⇒ `CartService.ClearAsync()` e `RedirectToPage("/Sales/Index")`). Exibe `TempData` (ex.: "Venda cancelada."). | | |
| TASK-022 | `Pages/Sales/Index` (`/Sales`): tabela de produtos (sabor, tipo, preço `R$`, estoque, selo "ESTOQUE BAIXO") com formulário por linha (`ProductId` hidden + `Quantity`, `OnPostAddItemAsync`); cartão do carrinho com itens e subtotal; botões "Finalizar" (⇒ `/Sales/Checkout`) e "Cancelar venda" (⇒ `/Sales/Cancel`). Erros FE01/FE02 via `ModelState`; sucesso ⇒ `RedirectToPage()`. | | |
| TASK-023 | `Pages/Sales/Cancel`: confirmação "Deseja realmente cancelar a venda?"; `OnPostAsync` ⇒ `CartService.ClearAsync`, `TempData["Success"] = "Venda cancelada."`, redireciona para `/Index`; "Não" ⇒ volta ao ponto de origem (`/Sales` ou `/Sales/Checkout`, via `returnPage` validado contra lista fixa). | | |
| TASK-024 | `Pages/Sales/Checkout`: carrinho vazio ⇒ redireciona a `/Sales`; resumo do carrinho; `InputModel` (`PaymentMethod` obrigatório; `AmountPaid` como `string`, `type="text" inputmode="decimal" placeholder="0,00"`, obrigatório se Dinheiro, convertido por `BrazilianMoney.TryParse`); `OnPostAsync` ⇒ `SaleService.CheckoutAsync`; sucesso ⇒ `RedirectToPage("Receipt", new { id })`. `BusinessException` (FE03 etc.) ⇒ `ModelState` e re-render mantendo os dados. Campo de valor visível só para Dinheiro (JS mínimo inline). Link "Cancelar venda". | | |
| TASK-025 | `Pages/Sales/Receipt` (`/Sales/Receipt?id=`): recibo com itens, total, valor pago, troco (Dinheiro), forma, data/hora e status "Concluída", valores em `R$` pt-BR; botão "Nova venda" (⇒ `/Index`). Id inexistente ⇒ `NotFound()`. | | |
| TASK-026 | `Program.cs` final: `AddSingleton<IProductRepository, CsvProductRepository>`, `AddSingleton<ISaleRepository, InMemorySaleRepository>`, `AddScoped<ICartStore, SessionCartStore>`, `AddScoped` para `ProductService`, `CartService`, `PaymentService`, `SaleService` (`SaleService` com o `SemaphoreSlim` estático); `/Error` já existente. | | |

### Implementation Phase 6 — Validação manual

- GOAL-006: Garantir comportamento equivalente ao Java (sem testes automatizados).

| Task | Description | Completed | Date |
| --- | --- | --- | --- |
| TASK-027 | Cenário Pix: Chocolate × 2 ⇒ total R$ 25,00; recibo exibido; `Data/produtos.csv` com Chocolate 10→8; log de bypass no console. | | |
| TASK-028 | Cenário Dinheiro (BRL): Morango × 3 (R$ 15,00); `10` ⇒ "Faltam: R$ 5,00."; `20,00` ⇒ troco R$ 5,00; `R$ 1.250,50` aceito; `20.00` e `abc` ⇒ "Valor inválido. Informe o valor no formato 0,00.". | | |
| TASK-029 | Cancelamento (FA04): adicionar itens → cancelar → "Não" (volta) → cancelar → "Sim" ⇒ "Venda cancelada.", volta ao início, estoque e CSV inalterados; repetir a partir do checkout. | | |
| TASK-030 | Erros: `ProductId` inexistente ⇒ FE01; quantidade > estoque (somando carrinho) ⇒ FE02; Baunilha (estoque 5) com selo "ESTOQUE BAIXO"; botão "Realizar Venda" sempre inicia carrinho vazio; `dotnet build` sem warnings. | | |

## 3. Alternatives

- **ALT-001**: Banco SQLite + EF Core — adiado (fora do escopo desta fase); a troca futura é uma nova implementação de `IProductRepository`/`ISaleRepository`.
- **ALT-002**: Carrinho persistido — descartado no MVP (sessão é suficiente).
- **ALT-003**: Strategy/gateway com interface para pagamentos — descartado (over-engineering); `PaymentService` com `switch` e Pix mockado.
- **ALT-004**: Vários projetos (Domain/Infra/Web) — descartado; fronteiras garantidas por pastas/namespaces.
- **ALT-005**: Bootstrap para o tema — descartado; um `site.css` próprio escuro é menor e mais simples.

## 4. Dependencies

- **DEP-001**: .NET SDK 10. Nenhum pacote NuGet adicional.

## 5. Files

- **FILE-001**: `app-controle-vendas-csharp.csproj`, `Program.cs`, `appsettings.json`, `Pages/Index`, `Pages/Shared/_Layout.cshtml` (editar); `Privacy`, `wwwroot/lib`, `_Layout.cshtml.css` (remover).
- **FILE-002**: Novos arquivos em `Domain/`, `Infrastructure/`, `Services/`, `Pages/Sales/`, `Data/produtos.csv`, `wwwroot/css/site.css`.
- **FILE-003**: `app-web-csharp/docs/guia_dev.md` (guia independente de feature).

## 6. Testing

Sem testes automatizados. Validação manual pelas TASK-027 a TASK-030; critério: todos os cenários coincidem com o comportamento do Java e o CSV reflete a baixa de estoque somente em vendas concluídas.

## 7. Risks & Assumptions

- **RISK-001**: Vendas em memória são perdidas ao reiniciar (igual ao Java). Aceito no MVP.
- **RISK-002**: Sessão em memória perde o carrinho ao reiniciar a aplicação. Aceito.
- **RISK-003**: Repositório CSV mantém os produtos em cache; edições manuais do arquivo com a aplicação rodando só valem após reiniciar.
- **RISK-004**: Vendas simultâneas — mitigado apenas por `SemaphoreSlim` (premissa single-user); com banco, trocar por controle de concorrência.
- **ASSUMPTION-001**: Autenticação e perfis são pré-condição e não implementados.
- **ASSUMPTION-002**: Pix mockado sempre aprova; FE04 só será implementado com gateway real.

## 8. Related Specifications / Further Reading

- `docs/spec.md` (UC01, RN01–RN05, seção 7)
- `docs/diagrama-sequencia-registrar-venda-no-carrinho-com-pix.txt`
- `docs/diagrama-sequencia-registrar-venda-no-carrinho-com-dinheiro.txt`
- `plan/feature-cart-sale-checkout-1.md` (versão Java)
- `app-web-csharp/docs/guia_dev.md`
