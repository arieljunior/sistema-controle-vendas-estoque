# Onboarding – Registrar Venda no Carrinho (SCVE)

Guia para quem está começando no projeto **Sistema de Controle de Vendas e Estoque**. Ao final você saberá onde cada coisa está, como o fluxo de venda funciona e quais regras de negócio são aplicadas.

> Fonte dos requisitos: [`docs/spec.md`](./spec.md) (caso de uso UC01 e regras RN01–RN05) e os diagramas de sequência desta pasta.

---

## 1. O que o sistema faz hoje

É uma aplicação de **terminal** (sem interface gráfica) para um vendedor ambulante de sorvetes. Neste MVP existe **um único fluxo**: registrar uma venda montando um carrinho e pagando em **Dinheiro** ou **Pix**.

- Produtos vêm do arquivo `produtos.csv` (na raiz do projeto).
- O estoque é atualizado no próprio CSV quando uma venda é concluída.
- As vendas ficam **somente em memória** (somem ao fechar o programa).
- O Pix é **mockado**: sempre aprova e registra um log de *bypass*.

## 2. Como rodar

Requisitos: **JDK 21**. Execute a partir da raiz do projeto (o programa lê `produtos.csv` do diretório atual).

```powershell
# compilar
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src\main\java -Filter *.java).FullName

# executar
java -Dfile.encoding=UTF-8 -cp out org.example.Main
```

Com Maven instalado: `mvn compile` e execute a classe `org.example.Main`.

> Atenção: vender de verdade **altera o `produtos.csv`**. Faça uma cópia antes de testar.

## 3. Formato do `produtos.csv`

```
id;nomeSabor;tipo;precoUnitario;quantidadeEstoque
1b6f0c1e-3a5d-4c7e-9d11-0a1b2c3d4e01;Chocolate;Massa;12.5;50
```

- Separador `;`, codificação **UTF-8**, ponto como separador decimal.
- `id` é um UUID; `tipo` é `Massa` ou `Picolé`.
- O cabeçalho é ignorado na leitura (as colunas são lidas **por posição**) e regravado com o mesmo texto ao salvar.

---

## 4. Arquitetura: MVC em camadas (View → Controller → Service → Repository)

A regra de ouro: **cada camada só conversa com a camada logo abaixo** e tem uma única responsabilidade.

```
┌──────────┐   ┌─────────────┐   ┌──────────┐   ┌────────────┐
│   View   │ → │ Controller  │ → │ Service  │ → │ Repository │ → produtos.csv
│ (console)│   │ (orquestra) │   │ (regras) │   │ (dados)    │
└──────────┘   └─────────────┘   └──────────┘   └────────────┘
                         todas usam o  Model (entidades)
```

| Camada | Pacote | Responsabilidade | Não deve |
| --- | --- | --- | --- |
| **View** | `view` | Mostrar informações e **ler** o que o usuário digita (`System.out` / `Scanner`). | Ter regra de negócio. |
| **Controller** | `controller` | Conduzir o fluxo do caso de uso: chama a view, chama os services e trata erros. | Calcular valores ou mexer em arquivo. |
| **Service** | `service` | **Regras de negócio** (validar estoque, pagar, baixar estoque). | Imprimir ou ler do terminal. |
| **Repository** | `repository` | Ler/gravar dados (CSV de produtos, lista de vendas em memória). | Conter regra de negócio. |
| **Model** | `model` | Entidades e valores do domínio, com suas próprias validações. | Depender de outras camadas. |

### Mapa dos arquivos (`src/main/java/org/example/`)

| Arquivo | O que faz |
| --- | --- |
| `Main` | *Composition root*: cria os objetos e liga tudo (injeção de dependência manual). |
| `controller/MenuController` | Menu principal (`1 - Registrar Venda`, `0 - Sair`). |
| `controller/SalesController` | Fluxo completo do UC01 (carrinho → pagamento → recibo / cancelamento). |
| `view/SalesView` | Todas as telas e perguntas do fluxo de venda. |
| `service/ProductService` | Lista produtos e busca por id. |
| `service/CartService` | Adiciona produto ao carrinho. |
| `service/PaymentService` | Processa Dinheiro (valida e calcula troco) e Pix (bypass). |
| `service/SaleService` | Finaliza a venda: valida, paga, baixa estoque e registra. |
| `repository/ProductRepository` | **Interface** de acesso a produtos. |
| `repository/CsvProductRepository` | Implementação que usa o `produtos.csv`. |
| `repository/SaleRepository` | Guarda as vendas concluídas em memória. |
| `model/Product`, `Cart`, `CartItem`, `Sale`, `SaleItem` | Entidades. |
| `model/ProductType`, `PaymentMethod`, `SaleStatus` | Enums (com o valor da spec em `getCode()`). |
| `model/Money` | Utilitário: valores monetários com 2 casas decimais. |

### Conceitos de orientação a objetos aplicados

- **Encapsulamento**: o estoque do `Product` só muda por `decreaseStock`/`increaseStock`; atributos são `private`.
- **Invariantes no construtor**: `Product` não aceita preço ≤ 0, estoque negativo ou nome vazio/maior que 50 caracteres. `SaleItem` não aceita quantidade ≤ 0.
- **Programar para interface**: `ProductService` usa `ProductRepository` (interface). Trocar CSV por banco de dados significa criar outra implementação, sem mexer nos services.
- **Injeção de dependência**: classes recebem o que precisam pelo construtor; quem monta tudo é o `Main`.
- **Imutabilidade**: `Sale` e `SaleItem` guardam o preço do momento da venda (`unitPriceAtSale`); listas expostas são não modificáveis.
- **Enums com código da spec**: `PaymentMethod.CASH.getCode()` devolve `DINHEIRO`; `SaleStatus.COMPLETED` devolve `CONCLUIDA`.
- **`BigDecimal` para dinheiro**: nunca use `double` para valores monetários (erros de arredondamento). `Money.of(...)` padroniza para 2 casas (`HALF_UP`).
- **Exceções padrão**: violações de regra viram `IllegalArgumentException` com a mensagem em português para o usuário; erros de arquivo viram `UncheckedIOException`/`IllegalStateException`.

---

## 5. Fluxo "Registrar Venda no Carrinho"

### Passo a passo (caminho feliz)

1. `Main` cria os objetos e inicia o `MenuController`.
2. Usuário escolhe **1 - Registrar Venda** → `SalesController.run()`.
3. A view lista os produtos numerados (1..N), com preço e estoque; produtos com estoque ≤ 5 recebem `<< ESTOQUE BAIXO`.
4. Usuário informa o **número do produto** e a **quantidade**. O controller traduz o número para o produto e chama `CartService.addItem` → `Cart.addItem` (valida estoque).
5. O carrinho e o subtotal são exibidos. Usuário escolhe: **1** adicionar mais itens, **2** finalizar, **3** cancelar.
6. Ao finalizar, escolhe a forma de pagamento: **1 - Dinheiro**, **2 - Pix** ou **0 - Cancelar**.
7. Se for Dinheiro, informa o valor pago.
8. `SaleService.checkout(...)` executa a venda (ver abaixo).
9. A view mostra o **recibo** e o sistema volta ao menu.

### O que acontece dentro de `SaleService.checkout`

```
carrinho vazio?  ──sim──▶ erro
revalida estoque de cada item no repositório
cria Sale (PENDENTE) a partir do carrinho
PaymentService.process(...)   ← pode lançar erro (ex.: dinheiro insuficiente)
         │ só se o pagamento for aprovado:
baixa o estoque dos produtos e grava o produtos.csv
Sale.complete(valorPago, troco)  → status CONCLUIDA
SaleRepository.save(sale)
```

Se a gravação do CSV falhar, o estoque em memória é **restaurado** e o erro é exibido (a venda não é registrada).

### Fluxos alternativos e de erro

| Situação | Comportamento |
| --- | --- |
| Número de produto inexistente | `Produto não encontrado. Verifique o código digitado.` – o carrinho é mantido. |
| Quantidade maior que o estoque (somando o que já está no carrinho) | `Estoque insuficiente para o produto X. Quantidade disponível em estoque: N.` – item descartado. |
| Quantidade ≤ 0 ou texto não numérico | A view/regra rejeita e pede novamente. |
| Finalizar com carrinho vazio | `O carrinho está vazio. Adicione ao menos um item.` |
| Dinheiro menor que o total | `Valor informado é insuficiente ... Faltam: R$ X.` – pede o pagamento de novo. |
| **Cancelar venda** (item 3 do menu de itens, `0` na forma de pagamento ou `C` no valor em dinheiro) | Pede confirmação `S/N`. Se `S`: limpa o carrinho, **não altera estoque**, não registra venda e volta ao menu. Se `N`: retorna de onde parou. |
| Opção de menu inválida | Mostra aviso e reexibe o menu. |

---

## 6. Regras de negócio implementadas

| Regra | Descrição | Onde está |
| --- | --- | --- |
| **RN01** – Disponibilidade de estoque | Só adiciona ao carrinho se a quantidade total pedida (já no carrinho + nova) for ≤ estoque. | `Cart.addItem`; revalidada em `SaleService.validateStock`. |
| **RN02** – Pix depende do gateway | No MVP o gateway é substituído por **bypass**: aprova sempre e escreve o log `PIX gateway BYPASS enabled...`. | `PaymentService.payWithPix` |
| **RN03** – Baixa automática de estoque | Estoque só é reduzido **depois** da confirmação do pagamento e gravado no CSV. | `SaleService.applyStockDeduction` |
| **RN04** – Valor mínimo em dinheiro | Valor pago deve ser ≥ total da compra. | `PaymentService.payWithCash` |
| **RN05** – Cálculo do troco | `troco = valorPago − valorTotal`. No Pix não há troco. | `PaymentService.payWithCash` |
| **RN06** – Alerta de estoque baixo | Estoque ≤ 5 é sinalizado na listagem. | `Product.isLowStock`, `SalesView.showProducts` |
| **RF06** – Impedir venda sem estoque | Consequência de RN01. | `Cart.addItem` |

Regras de dados (spec, seção 7): preço > 0 e estoque ≥ 0 (`Product`); quantidade do item > 0 (`SaleItem`); valores com 2 casas decimais (`Money`); status da venda: `PENDENTE` → `CONCLUIDA` (`CANCELADA` existe no enum, mas cancelar antes do pagamento não gera registro de venda).

---

## 7. Como evoluir o projeto

- **Nova opção de menu** (ex.: consultar estoque): crie um controller e inclua uma linha em `MenuController.readChoice()` e um `case` em `execute()`.
- **Pix real**: substitua o corpo de `PaymentService.payWithPix` pela chamada ao gateway (mantendo a assinatura) e trate a recusa lançando `IllegalArgumentException` com o motivo.
- **Outro armazenamento**: implemente `ProductRepository` (ex.: banco de dados) e troque a instância criada em `Main`.
- **Fora do escopo atual**: alterar/remover item do carrinho, desconto, relatório diário, cadastro de produto, autenticação e persistência das vendas.

## 8. Dicas para quem está começando

1. Leia na ordem: `Main` → `MenuController` → `SalesController` → `SaleService` → `PaymentService` → `Cart`/`Product`.
2. Ao mexer numa regra, pergunte: *"isso é regra de negócio?"* Se sim, vai no **service** (ou no **model**), nunca na view.
3. Mensagens ao usuário ficam em português (conforme a spec); nomes de classes, métodos e variáveis ficam em **inglês**.
4. Use sempre `BigDecimal` para dinheiro e `Money.of(...)` para normalizar.
5. Depois de testar vendas reais, restaure o `produtos.csv` (`git checkout produtos.csv`).
