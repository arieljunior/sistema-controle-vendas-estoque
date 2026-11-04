---
goal: Implementar a estrutura base (View/Controller/Service/Repository), um menu de terminal extensível e o fluxo UC01 "Registrar Venda no Carrinho" com pagamento em Dinheiro ou Pix (gateway mockado), usando produtos.csv como fonte de dados
version: 1.2
date_created: 2026-11-03
last_updated: 2026-11-03
owner: Equipe SCVE
status: 'Planned'
tags: [feature, architecture, java, cli, oop, mvp]
---

# Introdução

![Status: Planned](https://img.shields.io/badge/status-Planned-blue)

Plano simplificado para o MVP de linha de comando do SCVE com **um único fluxo**: UC01 (fluxo principal com Pix e FA01 com Dinheiro), conforme `docs/spec.md` e os diagramas de sequência em `docs/`. Produtos vêm de `produtos.csv`; vendas ficam em memória. Há um menu de terminal controlado por um registro de opções, pronto para receber novas funcionalidades. Sem testes unitários; a validação é manual. Código em inglês; mensagens ao usuário em pt-BR conforme a spec.

## 1. Requirements & Constraints

- **REQ-001**: Cobrir RF04, RF05, RF06, RF09 e RN01–RN05 da spec.
- **REQ-002**: Fluxo: listar produtos → adicionar itens (código + quantidade, repetível) → escolher pagamento → concluir → exibir recibo. **Cancelamento (FA04)** disponível a qualquer momento antes da confirmação do pagamento (menu de itens, escolha de forma de pagamento e entrada do valor em dinheiro).
- **REQ-008 (FA04)**: Ao escolher "Cancelar Venda": o sistema pede confirmação; se confirmado, limpa o carrinho (`Cart.clear()`), não altera estoque, não persiste venda e retorna ao menu principal; se negado, retorna ao ponto de origem.
- **REQ-009 (Tipos conforme spec, seção 7)**: ver CON-004 a CON-005 e TASK-002 a TASK-005.
- **REQ-003**: Pix via `PixGateway` mockado (sem rede). O mock aprova por padrão e permite recusa; recusa gera FE04 (tentar novamente ou trocar a forma de pagamento).
- **REQ-004**: Dinheiro: pedir valor pago, validar `valorPago >= total` (RN04/FE03), calcular troco (RN05).
- **REQ-005**: Baixa de estoque só após confirmação do pagamento (RN03). Em falha, o estoque não muda.
- **REQ-006**: Entidades da spec: `Product`, `Sale`, `SaleItem`, enums `ProductType`, `PaymentMethod`, `SaleStatus`. `Cart` é objeto de domínio transitório.
- **REQ-007 (Menu)**: Menu em loop com opções "1 - Registrar Venda" e "0 - Sair". Cada opção é um `MenuOption` registrado em `MenuController`; adicionar funcionalidade = criar uma classe e registrá-la no `Main`, sem alterar o laço do menu. Entrada inválida mostra aviso e reexibe o menu.
- **CON-001**: Java 21, Maven, somente JDK (sem frameworks, sem dependência de teste).
- **CON-002**: Camadas: `view` → `controller` → `service` → `repository`/`gateway`. View sem regra de negócio; service sem I/O de console.
- **CON-003**: Valores monetários com `BigDecimal` (escala 2, `HALF_UP`).
- **CON-004**: `produtos.csv`: separador `;`, UTF-8, ponto decimal, cabeçalho `id;nomeSabor;tipo;precoUnitario;quantidadeEstoque` (leitura por posição). Tipo `Massa`/`Picolé` ↔ `ProductType` (sem acento e sem caixa).
- **CON-005 (Tipos conforme spec)**: todos os ids são `UUID` (36 caracteres), inclusive `Product.id`. `produtos.csv` será migrado: os ids 1, 2, 3 são substituídos por UUIDs fixos (TASK-020). Como o usuário digita um "código/número" (UC01 passo 3), a view numera os produtos listados (1..N, ordem do CSV) e o controller converte o número no `Product` correspondente (número inexistente ⇒ FE01). Demais tipos: `flavorName` String ≤ 50 e único (case-insensitive); `type` enum; `unitPrice` `BigDecimal` escala 2 (10,2) > 0; `stockQuantity` int ≥ 0; `Sale.dateTime` `LocalDateTime`; `Sale.totalAmount` `BigDecimal` (10,2); `Sale.amountPaid` `BigDecimal` (10,2), obrigatório (no Pix = total); `Sale.change` `BigDecimal` (10,2) anulável (null no Pix); `Sale.status` enum; `SaleItem.quantity` int > 0; `SaleItem.unitPriceAtSale` `BigDecimal` (10,2). Enums mantêm nomes em inglês no código e expõem o valor da spec via `getCode()` (ex.: `CASH.getCode()` = `DINHEIRO`); `ProductType`: `PICOLE`, `MASSA`.
- **CON-006**: Remover do `Main` atual a escrita que sobrescreve `produtos.csv`; apagar `Produto.java`.
- **CON-007 (Simplicidade)**: Fora de escopo: FA02, FA03, relatórios, cadastro, autenticação, persistência de vendas. Uma exceção base `BusinessException` e poucas subclasses; sem interfaces desnecessárias (apenas `ProductRepository`, `PixGateway`, `PaymentProcessor`, `MenuOption`).
- **GUD-001**: Nomes em inglês; classes pequenas e coesas; imutabilidade (`final`, `record`) onde couber; invariantes validadas no domínio.
- **PAT-001**: Repository. **PAT-002**: Strategy (`PaymentProcessor`). **PAT-003**: Port/Adapter (`PixGateway` + `MockPixGateway`). **PAT-004**: Command (`MenuOption`). **PAT-005**: DI manual no `Main`.

## 2. Implementation Steps

Pacote base `org.example`; caminho `src/main/java/org/example/`.

### Implementation Phase 1

- GOAL-001: Domínio e exceções.

| Task | Description | Completed | Date |
| --- | --- | --- | --- |
| TASK-001 | `pom.xml`: `artifactId` → `scve`; manter Java 21 e UTF-8. Criar pacotes `model`, `exception`, `repository`, `gateway`, `service`, `service.payment`, `controller`, `view`, `view.menu`. | | |
| TASK-002 | Enums com campo `code` (spec) e `getCode()`: `model/ProductType` (`PICOLE`, `MASSA`; `fromLabel(String)` aceita `Massa`/`Picolé` do CSV sem acento/caixa e `getLabel()`), `model/PaymentMethod` (`CASH`→`DINHEIRO`, `PIX`→`PIX`), `model/SaleStatus` (`PENDING`→`PENDENTE`, `COMPLETED`→`CONCLUIDA`, `CANCELED`→`CANCELADA`). | | |
| TASK-003 | `model/Product` (`UUID id`, `String flavorName` ≤ 50, `ProductType type`, `BigDecimal unitPrice` escala 2 > 0, `int stockQuantity` ≥ 0; validação no construtor): `isLowStock()` (≤ 5), `hasStockFor(int)`, `decreaseStock(int)`. | | |
| TASK-004 | `model/Cart` + `CartItem` (product, quantity, `subtotal()`): `addItem(Product,int)` valida quantidade > 0 e RN01 somando o que já está no carrinho; `total()`, `isEmpty()`, `clear()` (FA04), `getItems()` imutável. | | |
| TASK-005 | `model/Sale` (`UUID id`, `LocalDateTime dateTime`, `PaymentMethod paymentMethod`, `BigDecimal totalAmount`, `BigDecimal amountPaid`, `BigDecimal change` anulável, `SaleStatus status`, `List<SaleItem> items`) e `SaleItem` (`UUID id`, `UUID saleId`, `UUID productId`, `int quantity` > 0, `BigDecimal unitPriceAtSale`, `subtotal()`). Todos os `BigDecimal` normalizados para escala 2 `HALF_UP`. `Sale.fromCart(Cart, PaymentMethod)` cria PENDING com `LocalDateTime.now()`; `complete(amountPaid, change)` define CONCLUIDA. | | |
| TASK-006 | `exception/`: `BusinessException` (base, RuntimeException), `ProductNotFoundException` (FE01), `InsufficientStockException` (FE02, nome e saldo), `InsufficientCashException` (FE03, diferença), `PixPaymentRejectedException` (FE04, motivo), `EmptyCartException`, `DataAccessException` (I/O do CSV). | | |

### Implementation Phase 2

- GOAL-002: Persistência CSV e Pix mockado.

| Task | Description | Completed | Date |
| --- | --- | --- | --- |
| TASK-007 | `repository/ProductRepository` (`findAll()`, `findById(UUID)`, `saveAll(List<Product>)`) e `CsvProductRepository(Path)`: lê em UTF-8, ignora cabeçalho e linhas vazias, converte `id` com `UUID.fromString`, preço com `new BigDecimal(...)`, erro de formato (incluindo UUID inválido) ⇒ `DataAccessException` com a linha; `saveAll` regrava com o mesmo cabeçalho em UTF-8. Mantém lista em memória carregada na construção. | | |
| TASK-008 | `repository/SaleRepository` (classe concreta simples, lista em memória: `save(Sale)`, `findAll()`). | | |
| TASK-009 | `gateway/PixGateway` (`PixChargeResult charge(BigDecimal)`), `gateway/PixChargeResult` (record: approved, transactionId, reason) e `gateway/MockPixGateway` (aprova gerando UUID; flag `approveAlways` configurável no construtor para demo de recusa). | | |

### Implementation Phase 3

- GOAL-003: Services e pagamento.

| Task | Description | Completed | Date |
| --- | --- | --- | --- |
| TASK-010 | `service/ProductService`: `listProducts()`, `getById(int)` (lança `ProductNotFoundException`). `service/CartService`: `addItem(Cart, int productId, int quantity)`. | | |
| TASK-011 | `service/payment/PaymentRequest` (record: amountPaid nullable), `PaymentResult` (record: amountPaid, change), interface `PaymentProcessor` (`PaymentMethod supports()`, `PaymentResult process(BigDecimal total, PaymentRequest)`). | | |
| TASK-012 | `CashPaymentProcessor` (RN04 ⇒ `InsufficientCashException`; troco RN05) e `PixPaymentProcessor` (chama `PixGateway`; recusa ⇒ `PixPaymentRejectedException`; amountPaid = total, change = 0.00). | | |
| TASK-013 | `service/SaleService(ProductRepository, SaleRepository, List<PaymentProcessor>)`: `checkout(Cart, PaymentMethod, PaymentRequest)`: valida carrinho não vazio; cria `Sale`; processa pagamento; só em sucesso baixa estoque, `saveAll`, `sale.complete`, `saleRepository.save` (RN03). Em cancelamento nenhuma chamada a `SaleService` é feita (o controller apenas limpa o carrinho via `Cart.clear()`). | | |

### Implementation Phase 4

- GOAL-004: View, controllers e menu extensível.

| Task | Description | Completed | Date |
| --- | --- | --- | --- |
| TASK-014 | `view/ConsoleView(Scanner, PrintStream)`: `print`, `printError`, `readInt(prompt)`, `readMoney(prompt)` (reprompt em entrada inválida). | | |
| TASK-015 | `view/SalesView`: `showProducts` (marca "ESTOQUE BAIXO"), `showCart`, `askProductCode` (número exibido na listagem), `askQuantity`, `askNextAction` (1 adicionar, 2 finalizar, 3 cancelar venda), `askPaymentMethod` ("1 - Dinheiro", "2 - Pix", "0 - Cancelar venda"), `askCashAmount` (permite cancelar), `askPixRetryOrChange` (FE04), `confirmCancellation()` (FA04), `showReceipt(Sale)` (exibe `dateTime` formatada e ids). | | |
| TASK-016 | `view/menu/MenuOption` (interface: `int getCode()`, `String getLabel()`, `void execute()`) e `view/menu/MenuView`: exibe título e opções ordenadas por código e lê a escolha. | | |
| TASK-017 | `controller/SalesController implements MenuOption` (código 1, "Registrar Venda"): executa UC01 passos 2–11; captura `BusinessException` e exibe mensagens FE01–FE04 como na spec; `InsufficientCashException` pede o valor novamente; `PixPaymentRejectedException` oferece tentar de novo ou trocar para Dinheiro. FA04: nos pontos de cancelamento (REQ-008) chama `confirmCancellation()`; se confirmado, `cart.clear()`, exibe "Venda cancelada." e retorna ao menu. Mapeia número digitado → `Product` pela lista de `ProductService.listProducts()`. | | |
| TASK-018 | `controller/MenuController`: recebe `MenuView` e `List<MenuOption>`; `register(MenuOption)` rejeita código duplicado; `run()` em loop até a opção 0 (opção "Sair" embutida); opção inexistente ⇒ aviso e reexibe; exceção inesperada em uma opção é capturada, exibida e o menu continua. | | |
| TASK-019 | `Main`: composition root — `CsvProductRepository(Path.of("produtos.csv"))`, `SaleRepository`, `MockPixGateway`, processors, services, views, `SalesController`; `menuController.register(salesController)`; `menuController.run()`. Console em UTF-8. | | |

### Implementation Phase 5

- GOAL-005: Validação manual (sem testes unitários).

| Task | Description | Completed | Date |
| --- | --- | --- | --- |
| TASK-020 | Migrar `produtos.csv`: substituir ids `1`,`2`,`3` por UUIDs fixos (um por linha), corrigir `PicolÃ©` para `Picolé` (RISK-001), manter UTF-8 e cabeçalho. `mvn -q compile` sem erros. | | |
| TASK-021 | Cenário MVP Pix: menu → 1 → produto 1, qtd 2 → finalizar → "2 - Pix" ⇒ recibo total 25.00 e estoque do Chocolate 50→48 no CSV. | | |
| TASK-022 | Cenário Dinheiro: produto 2, qtd 3 → "1 - Dinheiro", pago 10.00 ⇒ FE03 (faltam R$ 5.00); pago 20.00 ⇒ troco 5.00. | | |
| TASK-024 | Cenário cancelamento (FA04): adicionar itens → opção 3 → negar confirmação (volta ao carrinho) → opção 3 → confirmar ⇒ "Venda cancelada.", retorno ao menu, `produtos.csv` inalterado; repetir cancelando na tela de forma de pagamento e na entrada do valor em dinheiro. | | |
| TASK-023 | Cenários de erro: código 99 ⇒ FE01; quantidade acima do estoque ⇒ FE02; opção de menu inválida ⇒ aviso; opção 0 encerra. | | |

## 3. Alternatives

- **ALT-001**: Spring Boot/JPA — descartado (CLI standalone, CSV como fonte).
- **ALT-002**: `switch` fixo no `Main` para o menu — descartado; `MenuOption` permite expansão sem mexer no laço.
- **ALT-003**: `if/else` por pagamento — descartado em favor de Strategy.

## 4. Dependencies

- **DEP-001**: JDK 21, Maven 3.9+.
- **DEP-002**: `produtos.csv` na raiz do projeto (diretório de execução).

## 5. Files

- **FILE-001**: `pom.xml` (editar). **FILE-002**: `Main.java` (reescrever); `Produto.java` (remover).
- **FILE-003**: Novos arquivos em `src/main/java/org/example/{model,exception,repository,gateway,service,service/payment,controller,view,view/menu}`.
- **FILE-004**: `produtos.csv` (atualizado em runtime).

## 6. Testing

Sem testes unitários. Validação manual por TASK-020 a TASK-023; critério: todos os cenários produzem o resultado esperado e o CSV reflete a baixa de estoque somente nas vendas concluídas.

## 7. Risks & Assumptions

- **RISK-001**: O `Main` atual gravou `PicolÃ©` (encoding errado) em `produtos.csv`; corrigir o arquivo para `Picolé` em UTF-8 antes de executar.
- **RISK-002**: Console Windows pode exibir acentos errados; usar `PrintStream` UTF-8 explícito.
- **ASSUMPTION-001**: Autenticação e perfis são pré-condição e não implementados.
- **ASSUMPTION-002**: Pix mockado é síncrono; uso single-user.

## 8. Related Specifications / Further Reading

- `docs/spec.md` (UC01, RN01–RN05, seção 7)
- `docs/diagrama-sequencia-registrar-venda-no-carrinho-com-pix.txt`
- `docs/diagrama-sequencia-registrar-venda-no-carrinho-com-dinheiro.txt`

