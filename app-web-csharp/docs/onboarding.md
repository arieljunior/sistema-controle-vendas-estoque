# Onboarding — aplicação web de vendas em C#

Este documento descreve a arquitetura da aplicação web do Sistema de Controle de Vendas e Estoque, suas responsabilidades por camada e a implementação do fluxo de venda. Também registra os procedimentos de execução local e as limitações atuais do MVP.

## 1. O que a aplicação faz

O projeto é uma aplicação web ASP.NET Core com Razor Pages. O usuário consulta produtos e estoque, monta um carrinho, informa uma forma de pagamento e recebe um recibo.

Este é um MVP, sem banco de dados:

- Os produtos são carregados de [`Data/produtos.csv`](../app-controle-vendas-csharp/Data/produtos.csv). O repositório mantém os produtos em memória enquanto a aplicação está rodando e grava o CSV quando o estoque muda.
- O carrinho é guardado na sessão HTTP, usando armazenamento em memória do servidor.
- As vendas concluídas são guardadas somente em memória. Elas desaparecem quando o processo da aplicação é reiniciado.
- Dinheiro é validado localmente; o Pix é simulado e aprovado automaticamente. Não há integração com banco ou gateway de pagamento.
- Não há projeto de testes automatizados nesta fase; consulte os cenários manuais mais adiante.

> **Cuidado ao testar:** uma venda concluída altera `Data/produtos.csv`. Faça uma cópia do arquivo antes de testar se quiser preservar o estoque original.

## 2. Executar localmente

É necessário ter o **.NET SDK 10** instalado, pois o projeto mira `net10.0`.

No PowerShell, a partir da pasta `app-web-csharp`:

```powershell
dotnet --version
dotnet build .\app-controle-vendas-csharp\app-controle-vendas-csharp.csproj
dotnet run --project .\app-controle-vendas-csharp\app-controle-vendas-csharp.csproj --launch-profile http
```

Abra <http://localhost:5247>. O perfil `http` é definido em [`launchSettings.json`](../app-controle-vendas-csharp/Properties/launchSettings.json). A aplicação usa `Data:ProductsCsvPath` em [`appsettings.json`](../app-controle-vendas-csharp/appsettings.json) para localizar o CSV, relativo à pasta do projeto.

## 3. Mapa do projeto

O código executável está em um único projeto, [`app-controle-vendas-csharp`](../app-controle-vendas-csharp/). As pastas separam responsabilidades; elas não são projetos C# independentes.

```text
app-controle-vendas-csharp/
├── Domain/
│   ├── Entities/       # Product, Cart, Sale e suas regras/invariantes
│   ├── Enums/          # Tipos de produto, pagamentos e status
│   ├── Exceptions/     # Erros de regra de negócio
│   └── Interfaces/     # Contratos para produtos, vendas e carrinho
├── Infrastructure/
│   ├── Repositories/   # Acesso ao CSV e armazenamento de vendas em memória
│   └── Session/        # Adaptador do carrinho para a sessão HTTP
├── Services/           # Casos de uso e regras que coordenam as entidades
│   └── Models/         # Dados de saída preparados para as páginas
├── Pages/              # Páginas Razor: interface e handlers HTTP
├── Data/               # CSV de produtos
└── Program.cs          # Inicialização e configuração das dependências
```

### Como as camadas se relacionam

```text
Pages  →  Services  →  Domain
                       ↑
                 Infrastructure

Program.cs registra e conecta as implementações
```

- **Pages** recebem requisições e exibem a interface. Não devem implementar regras de negócio nem acessar arquivos ou repositórios diretamente.
- **Services** coordenam um caso de uso: carregam dados, chamam regras do domínio e pedem persistência pelas interfaces.
- **Domain** contém entidades e regras centrais, como calcular o total e impedir estoque insuficiente. Não depende de ASP.NET ou de arquivos.
- **Infrastructure** implementa os detalhes externos: CSV, memória e sessão. Ela cumpre contratos definidos no Domain.
- **Program.cs** é o ponto de composição: registra as implementações e informa ao ASP.NET Core quais classes fornecer quando outras classes precisarem delas.

### Elementos da implementação

- Um arquivo `.cshtml` contém o HTML da página, com trechos Razor para exibir dados. O arquivo `.cshtml.cs` ao lado é o `PageModel`, que carrega os dados e trata ações do usuário.
- `OnGetAsync` trata a abertura da página; `OnPost...Async` trata formulários enviados pelo navegador. `async`/`await` permitem aguardar operações de I/O sem bloquear a requisição.
- Um construtor como `CheckoutModel(CartService cartService, SaleService saleService)` recebe dependências. O contêiner configurado em `Program.cs` cria e fornece essas classes; isso é **injeção de dependência**.
- `Task` representa uma operação assíncrona. O sufixo `Async` no nome ajuda a identificar métodos que podem precisar ser aguardados com `await`.
- `Guid` é o identificador único usado para produtos e vendas. `decimal` é usado para dinheiro, evitando os erros de precisão comuns em tipos de ponto flutuante como `double`.
- `record` é usado nos modelos de saída para transportar dados da aplicação para as páginas, por exemplo `CartView` e `ReceiptView`.
- `[BindProperty]` permite que valores enviados pelo formulário sejam vinculados a propriedades do `PageModel`; atributos como `[Required]` e `[Range]` ajudam a validar entradas.

## 4. Como acompanhar o fluxo “Realizar venda”

Uma boa forma de estudar o fluxo é abrir os arquivos nesta ordem:

1. [`Pages/Sales/Index.cshtml`](../app-controle-vendas-csharp/Pages/Sales/Index.cshtml) — tela que mostra produtos, estoque e carrinho. Cada formulário de produto envia a ação `AddItem`.
2. [`Pages/Sales/Index.cshtml.cs`](../app-controle-vendas-csharp/Pages/Sales/Index.cshtml.cs) — `IndexModel`, o handler da página. Valida a quantidade, chama `CartService` e recarrega a tela.
3. [`Services/CartService.cs`](../app-controle-vendas-csharp/Services/CartService.cs) — localiza o produto, atualiza o carrinho e transforma os dados em `CartView` para a interface.
4. [`Domain/Entities/Cart.cs`](../app-controle-vendas-csharp/Domain/Entities/Cart.cs) — mantém itens e total, junta quantidades do mesmo produto e impede adicionar mais do que o estoque disponível.
5. [`Infrastructure/Session/SessionCartStore.cs`](../app-controle-vendas-csharp/Infrastructure/Session/SessionCartStore.cs) — salva na sessão apenas o identificador do produto e a quantidade. Ao carregar o carrinho, o serviço busca os produtos atuais novamente.
6. [`Pages/Sales/Checkout.cshtml`](../app-controle-vendas-csharp/Pages/Sales/Checkout.cshtml) e [`Pages/Sales/Checkout.cshtml.cs`](../app-controle-vendas-csharp/Pages/Sales/Checkout.cshtml.cs) — exibem o resumo e recebem forma de pagamento e, para dinheiro, o valor pago. A página verifica campos e interpreta o valor no formato brasileiro.
7. [`Services/SaleService.cs`](../app-controle-vendas-csharp/Services/SaleService.cs) — coordena a finalização: valida o carrinho e o estoque novamente, cria a venda, processa o pagamento, baixa o estoque, registra a venda e limpa o carrinho.
8. [`Services/PaymentService.cs`](../app-controle-vendas-csharp/Services/PaymentService.cs) — calcula o troco ou aprova o Pix simulado.
9. [`Domain/Entities/Sale.cs`](../app-controle-vendas-csharp/Domain/Entities/Sale.cs) — cria os itens da venda com nome e preço unitário registrados naquele momento, totaliza e marca a venda como concluída.
10. [`Pages/Sales/Receipt.cshtml.cs`](../app-controle-vendas-csharp/Pages/Sales/Receipt.cshtml.cs) e [`Pages/Sales/Receipt.cshtml`](../app-controle-vendas-csharp/Pages/Sales/Receipt.cshtml) — buscam a venda pelo identificador e exibem recibo, valores e troco.

Em forma resumida:

```text
POST Adicionar item
  → IndexModel
  → CartService
  → Cart + ICartStore (sessão)

POST Confirmar pagamento
  → CheckoutModel valida entrada
  → SaleService valida estoque
  → PaymentService calcula/valida pagamento
  → SaleService baixa estoque e registra venda
  → redireciona para Receipt com o Guid da venda
```

### O que acontece ao confirmar

`CheckoutModel` confere se o carrinho não está vazio, se a forma de pagamento foi informada e, para dinheiro, se o valor tem formato válido. A classe `SaleService` faz uma nova consulta ao estoque porque a quantidade pode ter mudado desde que o item foi colocado no carrinho.

Se o pagamento for em dinheiro, `PaymentService` exige valor suficiente e calcula o troco. Se for Pix, o serviço registra um aviso de bypass no log e considera o valor pago igual ao total. Em seguida, a venda é marcada como concluída, o estoque é salvo e a venda é adicionada ao repositório em memória. A página então redireciona para o recibo usando o `Guid` gerado para a venda.

Se uma regra de negócio falhar, ela lança `BusinessException`. A página de checkout captura essa exceção, apresenta a mensagem ao usuário e mantém a tela para correção. A exceção não substitui a validação do formulário: validações de entrada e regras de negócio ocorrem em pontos diferentes do fluxo.

O botão de cancelar limpa o carrinho e retorna ao início; ele não grava uma venda cancelada no repositório.

## 5. Onde ficam os dados e as configurações

- [`Data/produtos.csv`](../app-controle-vendas-csharp/Data/produtos.csv): dados iniciais e persistidos dos produtos. O separador é `;`; o preço no arquivo usa ponto decimal (por exemplo, `12.50`). A tela, por outro lado, apresenta valores em `pt-BR`.
- [`Infrastructure/Repositories/CsvProductRepository.cs`](../app-controle-vendas-csharp/Infrastructure/Repositories/CsvProductRepository.cs): lê o CSV na criação do repositório e salva a lista completa após a baixa do estoque.
- [`Infrastructure/Repositories/InMemorySaleRepository.cs`](../app-controle-vendas-csharp/Infrastructure/Repositories/InMemorySaleRepository.cs): mantém as vendas em uma lista protegida durante a execução atual.
- [`Infrastructure/Session/SessionCartStore.cs`](../app-controle-vendas-csharp/Infrastructure/Session/SessionCartStore.cs): serializa os itens do carrinho na sessão.
- [`Program.cs`](../app-controle-vendas-csharp/Program.cs): registra Razor Pages, sessão, cultura `pt-BR`, repositórios e serviços. Os repositórios de produtos/vendas são `Singleton`; os serviços e o adaptador de sessão são `Scoped`.

O `Singleton` de produtos vive durante o processo e mantém uma cópia em memória. O arquivo CSV também é atualizado para que o estoque sobreviva ao reinício. As vendas, ao contrário, não são gravadas em arquivo nem banco. O carrinho depende da sessão em memória do servidor, portanto não deve ser tratado como armazenamento permanente.

## 6. Cenários para explorar manualmente

Como não há testes automatizados configurados, use o navegador para observar o comportamento:

1. Adicione um produto em quantidade válida e confirme se o subtotal e o total aparecem no carrinho.
2. Tente adicionar quantidade maior que o estoque e confira a mensagem de validação.
3. Finalize uma venda em dinheiro com valor exato; repita com valor maior e confira o troco.
4. Tente pagar em dinheiro com valor menor que o total ou com formato inválido.
5. Finalize uma venda em Pix e observe que o fluxo aprova sem integração externa.
6. Após uma venda concluída, confira a redução do estoque na tela e no CSV. Restaure a cópia do CSV se quiser repetir os cenários com os dados iniciais.
7. Cancele um carrinho e confira que ele é limpo sem gerar recibo.

Para analisar a composição da aplicação, consulte [`Program.cs`](../app-controle-vendas-csharp/Program.cs). Para rastrear uma regra de negócio, siga o Service até a entidade do Domain correspondente. Para analisar uma página, consulte em conjunto os arquivos `.cshtml` e `.cshtml.cs`.
