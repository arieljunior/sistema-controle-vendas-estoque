# ESPECIFICAÇÃO DE REQUISITOS DE SOFTWARE (ERS)

**Nome do Projeto:** Sistema de Controle Vendas e Estoque  
**Sigla do Projeto:** SCVE

---

## Histórico de Revisões

| Data | Versão | Autor | Descrição da Mudança |
| :--- | :--- | :--- | :--- |
| 12/08/2026 | 0.1 | ariel.cjunior@prof.infnet.edu.br | Elaboração inicial dos requisitos |

---

## 1. Introdução

Este documento especifica os requisitos funcionais e não funcionais para o Sistema de Controle de Vendas e Estoque desenvolvido para o microempreendedor ambulante. O objetivo é orientar o desenvolvimento de uma versão mínima viável (MVP) executada via linha de comando (terminal) para organizar o estoque do carrinho, agilizar o registro de vendas diárias e fornecer relatórios financeiros e de consumo.

### 1.2 Escopo

O sistema abrange a gestão de produtos no carrinho, o registro imediato de transações de venda (dinheiro/Pix) com baixa automática de estoque, além da consolidação de relatórios diários de faturamento e produtos mais vendidos.

**Fora do escopo do MVP:** Integração com maquininhas de cartão, emissão de nota fiscal, interface gráfica (GUI) e suporte a múltiplos usuários.

---

## 2. Descrição Geral

O software é uma aplicação autônoma (standalone). Ele atuará como a ferramenta central do ADM para substituir o controle em cadernos de papel durante seu expediente na rua e no encerramento das vendas em casa.

### 2.2 Funções do Produto

* Cadastrar e atualizar os produtos/sabores disponíveis no carrinho.
* Consultar a quantidade atualizada do estoque.
* Alertar o usuário sobre produtos com estoque crítico/baixo.
* Registrar vendas informando produto, quantidade e método de pagamento.
* Calcular troco caso pagamento em dinheiro.
* Realizar baixa automática no estoque no momento da venda.
* Gerar relatório consolidado do dia com totais faturados por meio de pagamento e ranking de vendas.

### 2.3 Características dos Atores

* **[Administrador]:** Acesso total ao sistema.
* **[Atendente]:** Consultar Estoque, Registrar Venda.
* **[Fornecedor]:** Consultar o estoque, Repor estoque.
* **[Gateway de Pagamento]:** Processar pagamentos.

---

## 3. Requisitos Funcionais (RF)

| ID | Nome do Requisito | Descrição breve | Prioridade |
| :--- | :--- | :--- | :--- |
| **RF01** | Cadastrar Produto | O sistema deve permitir o cadastro de novos sorvetes/picolés informando: Nome/Sabor, Tipo (Picolé/Massa), Preço Unitário (R\$) e Quantidade Inicial no carrinho. | Alta |
| **RF02** | Consultar Estoque | O sistema deve listar todos os produtos cadastrados exibindo Sabor, Tipo, Preço e Quantidade Atual disponível. | Alta |
| **RF03** | Alerta de Estoque Baixo | O sistema deve sinalizar/destacar visualmente os produtos que atingirem uma quantidade menor ou igual a 5 unidades. | Média |
| **RF04** | Registrar Venda | O sistema deve permitir registrar uma venda selecionando o produto, a quantidade vendida e a forma de pagamento (Dinheiro ou Pix). | Alta |
| **RF05** | Baixa Automática de Estoque | Ao confirmar uma venda, o sistema deve reduzir imediatamente a quantidade vendida do estoque do produto correspondente. | Alta |
| **RF06** | Impedir Venda Sem Estoque | O sistema não deve permitir a venda de uma quantidade superior à disponível no estoque atual do produto, exibindo uma mensagem de aviso. | Alta |
| **RF07** | Relatório de Fechamento do Dia | O sistema deve exibir um resumo das vendas contendo: Total faturado em Dinheiro, Total faturado em Pix, Faturamento Total do dia e Total de itens vendidos. | Média |
| **RF08** | Identificar Campeões de Venda | O sistema deve apresentar a lista dos produtos/sabores mais vendidos do dia em ordem decrescente de quantidade. | Média |
| **RF09** | Calcular o troco | O sistema deve solicitar o valor pago em dinheiro e apresentar o troco para ser devolvido com base na quantidade quando o pagamento for em dinheiro. | Média |

---

## 4. Tabela Atores x Caso de Uso

**Legendas:**
* **Fornece:** Fornece informação
* **Consome:** Consome informação
* **N/A:** Não se aplica

| Caso de uso | Administrador | Atendente | Fornecedor | Gateway de pagamento |
| :--- | :--- | :--- | :--- | :--- |
| **Registrar Venda** | Fornece e consome | Fornece e consome | N/A | Fornece e consome |
| **Consultar Estoque** | Consome | Consome | Consome | N/A |
| **Cadastrar Produto** | Gera | N/A | N/A | N/A |
| **Relatório de Fechamento do Dia** | Consome | N/A | N/A | N/A |
| **Processar Pix** | Consome | Consome | N/A | Fornece e Consome |
| **Baixa Automática de Estoque** | Consome | Consome | N/A | N/A |

---

## 5. Casos de Uso

### UC01 - Registrar Venda no Carrinho

| Campo | Valor |
| :--- | :--- |
| **Identificador** | UC01 |
| **Nome** | Registrar Venda no Carrinho |
| **Ator Principal** | Administrador, Atendente |
| **Atores Secundários** | Gateway de Pagamento |
| **Versão** | 1.1 |

#### Descrição Geral
Este caso de uso permite que o Administrador e Atendente registre a venda de sorvetes e produtos correlatos por meio da montagem de um carrinho de compras. O sistema permite a inserção de itens, verificação automatizada de estoque, escolha da forma de pagamento (Dinheiro ou Pix), cálculo de troco quando aplicável, integração com Gateway de Pagamento para transações Pix e encerramento do pedido com baixa automática no estoque.

#### Pré-condições e Pós-condições
* **Pré-condições:**
    1. O Administrador e Atendente deve estar autenticado no sistema.
    2. O catálogo de produtos deve estar cadastrado e com os preços atualizados.
* **Pós-condições (Sucesso):**
  a. A venda é registrada com status "Concluído".  
  b. A quantidade dos itens vendidos é deduzida do estoque em tempo real.  
  c. O comprovante/recibo da venda é gerado para exibição/impressão.
* **Pós-condições (Falha):**
  a. A venda não é finalizada e nenhum item tem sua quantidade deduzida do estoque.  
  b. Nenhuma transação financeira é consumada no sistema.

#### Fluxo Principal (Caminho Feliz)
1. O Administrador ou Atendente seleciona a opção "Registrar Venda" no menu principal.
2. O sistema exibe a interface de venda com a lista de produtos disponíveis, preços unitários e o carrinho de compras vazio.
3. O Administrador informa o código/número do produto e a quantidade desejada.
4. O sistema verifica a disponibilidade de estoque [RN01].
5. O sistema adiciona o produto ao carrinho, atualiza o valor subtotal e exibe a lista atualizada de itens.
6. O Administrador ou Atendente sinaliza a inclusão de mais itens. O sistema repete os passos 3 a 5 até que o Administrador opte por prosseguir para o pagamento.
7. O Administrador ou Atendente solicita a finalização e seleciona a forma de pagamento "2 - Pix".
8. O sistema gera a solicitação de cobrança Pix e a envia para o Gateway de Pagamento [RN02].
9. O Gateway de Pagamento processa a cobrança, confirma a transação e envia a resposta de aprovação ao sistema.
10. O sistema confirma a transação, realiza a baixa no estoque dos itens vendidos [RN03], registra a venda e exibe a mensagem de sucesso e o resumo do pedido.
11. O caso de uso é encerrado.

#### Fluxos Alternativos (FA)

* **FA01 - Pagamento em Dinheiro**
    * *Ponto de Origem:* Passo 7 do Fluxo Principal.
    1. O Administrador ou Atendente seleciona a forma de pagamento "1 - Dinheiro".
    2. O sistema solicita o valor pago em cédulas/moedas pelo cliente.
    3. O Administrador ou Atendente informa o valor fornecido pelo cliente.
    4. O sistema valida se o valor informado é igual ou superior ao total da compra [RN04].
    5. O sistema calcula o valor do troco [RN05] e o exibe na tela para o Administrador ou Atendente.
    6. O sistema avança para o Passo 10 do Fluxo Principal.

* **FA02 - Alteração de Quantidade ou Remoção de Item do Carrinho**
    * *Ponto de Origem:* Passo 5 do Fluxo Principal.
    1. O Administrador ou Atendente seleciona um item já presente no carrinho de compras e opta por alterar a quantidade ou removê-lo.
    2. Se a opção for alteração de quantidade:
        * 2.1. O Administrador digita a nova quantidade.
        * 2.2. O sistema valida o estoque [RN01].
        * 2.3. O sistema recalcula o valor total do item e do carrinho.
    3. Se a opção for remoção:
        * 3.1. O sistema remove o item do carrinho e recalcula o valor total.
    4. O sistema retorna ao Passo 5 do Fluxo Principal.

* **FA03 - Inclusão de Desconto Autorizado**
    * *Ponto de Origem:* Passo 6 do Fluxo Principal.
    1. O Administrador informa um percentual ou valor fixo de desconto no total da venda.
    2. O sistema aplica o desconto, recalcula o valor final do carrinho e reexibe o resumo atualizado.
    3. O sistema retorna ao Passo 7 do Fluxo Principal.

* **FA04 - Cancelamento da Venda**
    * *Ponto de Origem:* A qualquer momento antes da confirmação do pagamento (Passos 1 a 7 do FP, FA01 ou FA03).
    1. O Administrador seleciona a opção "Cancelar Venda".
    2. O sistema solicita a confirmação do cancelamento.
    3. O Administrador confirma o cancelamento.
    4. O sistema limpa os dados do carrinho sem realizar alterações no estoque.
    5. O caso de uso é encerrado.

#### Fluxos de Exceção (FE)

* **FE01 - Produto Não Encontrado**
    * *Ponto de Origem:* Passo 3 do Fluxo Principal.
    1. O sistema identifica que o código informado pelo Administrador não corresponde a nenhum produto cadastrado no catálogo.
    2. O sistema exibe a mensagem de erro: *"Produto não encontrado. Verifique o código digitado."*
    3. O sistema mantém os itens previamente adicionados no carrinho e retorna ao Passo 3 do Fluxo Principal.

* **FE02 - Estoque Insuficiente**
    * *Ponto de Origem:* Passo 4 do Fluxo Principal ou Passo 2.2 do FA02.
    1. O sistema identifica que a quantidade solicitada do produto é maior do que o saldo atual em estoque [RN01].
    2. O sistema exibe a mensagem de erro: *"Estoque insuficiente para o produto [Nome do Produto]. Quantidade disponível em estoque: [Saldo]."*
    3. O sistema descarta a adição ou alteração do item e retorna ao Passo 3 do Fluxo Principal (ou Passo 1 do FA02).

* **FE03 - Valor Pago em Dinheiro Insuficiente**
    * *Ponto de Origem:* Passo 4 do FA01.
    1. O sistema identifica que o valor informado pelo Administrador é menor do que o total da venda [RN04].
    2. O sistema exibe a mensagem de erro: *"Valor informado é insuficiente para cobrir o total da venda. Faltam: R\$ [Diferença]."*
    3. O sistema retorna ao Passo 2 do FA01 para que o valor seja corrigido ou o pagamento seja alterado.

* **FE04 - Falha na Comunicação / Transação Pix Recusada**
    * *Ponto de Origem:* Passo 9 do Fluxo Principal.
    1. O Gateway de Pagamento retorna uma recusa de transação (ex: saldo insuficiente, expiração do QR Code) ou ocorre um estouro do tempo limite de resposta (timeout).
    2. O sistema exibe a mensagem de erro: *"Transação Pix não autorizada ou indisponível no momento. Motivo: [Motivo informado pelo Gateway/Timeout]."*
    3. O sistema oferece ao Administrador as opções de "Tentar Novamente via Pix" ou "Alterar Forma de Pagamento".
    4. Se o Administrador optar por tentar novamente, o sistema retorna ao Passo 8 do Fluxo Principal.
    5. Se o Administrador optar por alterar a forma de pagamento, o sistema retorna ao Passo 7 do Fluxo Principal.

---

### UC02 - Cadastrar Produto

| Campo | Valor |
| :--- | :--- |
| **Identificador** | UC02 |
| **Nome** | Cadastrar Produto |
| **Ator Principal** | Administrador |
| **Atores Secundários** | Nenhum |
| **Versão** | 1.0 |

#### Descrição Geral
Este caso de uso permite ao Administrador cadastrar novos produtos/sabores de sorvetes e picolés no sistema para ficarem disponíveis para venda no carrinho.

#### Pré-condições e Pós-condições
* **Pré-condições:**
    1. O Administrador deve estar autenticado no sistema.
* **Pós-condições (Sucesso):**
    1. O novo produto é registrado no catálogo com o estoque inicial e fica disponível para venda.
* **Pós-condições (Falha):**
    1. O produto não é cadastrado e nenhuma alteração é feita no banco de dados/sistema.

#### Fluxo Principal (Caminho Feliz)
1. O Administrador seleciona a opção "Cadastrar Produto" no menu do sistema.
2. O sistema solicita os dados do produto: Nome/Sabor, Tipo (Picolé/Massa), Preço Unitário (R\$) e Quantidade Inicial no carrinho.
3. O Administrador informa os dados solicitados.
4. O sistema valida os campos informados (preenchimento obrigatório e valores numéricos válidos).
5. O sistema verifica se já existe um produto com o mesmo Nome/Sabor cadastrado.
6. O sistema salva o novo produto no catálogo e exibe uma mensagem de confirmação de cadastro com sucesso.
7. O caso de uso é encerrado.

#### Fluxos Alternativos (FA)
* **FA01 - Cancelamento do Cadastro**
    * *Ponto de Origem:* Passo 3 do Fluxo Principal.
    1. O Administrador decide cancelar a operação digitando o comando/opção de cancelamento.
    2. O sistema descarta as informações digitadas e retorna ao menu principal.
    3. O caso de uso é encerrado.

#### Fluxos de Exceção (FE)
* **FE01 - Dados Inválidos ou Incompletos**
    * *Ponto de Origem:* Passo 4 do Fluxo Principal.
    1. O sistema identifica que o preço unitário ou a quantidade inicial contém valores negativos, nulos ou com formato inválido.
    2. O sistema exibe a mensagem de erro: *"Dados inválidos. O preço e a quantidade inicial devem ser valores numéricos maiores ou iguais a zero."*
    3. O sistema retorna ao Passo 2 do Fluxo Principal.

* **FE02 - Produto Já Cadastrado**
    * *Ponto de Origem:* Passo 5 do Fluxo Principal.
    1. O sistema identifica que já existe um produto cadastrado com o mesmo Nome/Sabor.
    2. O sistema exibe a mensagem de erro: *"Produto '[Nome do Sabor]' já cadastrado no sistema."*
    3. O sistema solicita se o Administrador deseja alterar o nome ou cancelar a operação.
    4. Se optar por alterar, o sistema retorna ao Passo 2 do Fluxo Principal.

---

### UC03 - Consultar Estoque

| Campo | Valor |
| :--- | :--- |
| **Identificador** | UC03 |
| **Nome** | Consultar Estoque |
| **Ator Principal** | Administrador, Atendente, Fornecedor |
| **Atores Secundários** | Nenhum |
| **Versão** | 1.0 |

#### Descrição Geral
Permite aos atores visualizar a lista atualizada de produtos disponíveis no carrinho, incluindo preço, tipo e a quantidade atual em estoque, com destaque/alerta visual para itens em nível crítico.

#### Pré-condições e Pós-condições
* **Pré-condições:**
    1. O ator deve estar autenticado no sistema.
* **Pós-condições (Sucesso):**
    1. A listagem completa do estoque é exibida para o usuário com a devida sinalização de estoque baixo, quando aplicável.
* **Pós-condições (Falha):**
    1. Nenhuma. (Operação apenas de leitura).

#### Fluxo Principal (Caminho Feliz)
1. O ator seleciona a opção "Consultar Estoque" no menu do sistema.
2. O sistema busca todos os produtos cadastrados no catálogo.
3. O sistema exibe a listagem contendo: Código, Nome/Sabor, Tipo (Picolé/Massa), Preço Unitário (R\$) e Quantidade Atual.
4. Para cada produto com Quantidade Atual menor ou igual a 5 unidades, o sistema aplica um alerta/sinalização visual de "Estoque Crítico/Baixo" [RN06].
5. O sistema aguarda a confirmação do ator para voltar ao menu.
6. O ator confirma e o caso de uso é encerrado.

#### Fluxos Alternativos (FA)
* **FA01 - Filtrar / Pesquisar Produto Específico**
    * *Ponto de Origem:* Passo 3 do Fluxo Principal.
    1. O ator informa o código ou nome de um produto para filtrar a busca.
    2. O sistema exibe apenas o produto correspondente com suas respectivas informações e alerta de estoque baixo (se aplicável).
    3. O sistema retorna ao Passo 5 do Fluxo Principal.

#### Fluxos de Exceção (FE)
* **FE01 - Nenhum Produto Cadastrado**
    * *Ponto de Origem:* Passo 2 do Fluxo Principal.
    1. O sistema verifica que não existem produtos cadastrados no banco de dados.
    2. O sistema exibe a mensagem: *"Nenhum produto cadastrado no estoque."*
    3. O caso de uso é encerrado.

---

### UC04 - Gerar Relatório de Fechamento do Dia

| Campo | Valor |
| :--- | :--- |
| **Identificador** | UC04 |
| **Nome** | Gerar Relatório de Fechamento do Dia |
| **Ator Principal** | Administrador |
| **Atores Secundários** | Nenhum |
| **Versão** | 1.0 |

#### Descrição Geral
Permite ao Administrador visualizar a consolidação financeira das vendas realizadas no dia, os totais agrupados por meio de pagamento (Dinheiro e Pix) e o ranking dos produtos mais vendidos.

#### Pré-condições e Pós-condições
* **Pré-condições:**
    1. O Administrador deve estar autenticado no sistema.
* **Pós-condições (Sucesso):**
    1. O relatório consolidado é exibido em tela para conferência.
* **Pós-condições (Falha):**
    1. Nenhuma. (Operação apenas de leitura).

#### Fluxo Principal (Caminho Feliz)
1. O Administrador seleciona a opção "Relatório de Fechamento do Dia" no menu do sistema.
2. O sistema busca todas as transações registradas na data atual com status "Concluído".
3. O sistema calcula:
    * Total Faturado em Dinheiro (R\$).
    * Total Faturado em Pix (R\$).
    * Faturamento Total do dia (Somatório Dinheiro + Pix).
    * Total Geral de itens vendidos.
4. O sistema gera a lista dos produtos/sabores mais vendidos do dia ordenados em ordem decrescente de quantidade.
5. O sistema exibe a consolidação dos totais e o ranking de produtos em tela.
6. O Administrador confirma a leitura e o caso de uso é encerrado.

#### Fluxos Alternativos (FA)
* **FA01 - Exportar/Salvar Relatório em Arquivo**
    * *Ponto de Origem:* Passo 5 do Fluxo Principal.
    1. O Administrador seleciona a opção "Exportar Relatório".
    2. O sistema gera um arquivo de texto (.txt ou .csv) contendo o resumo consolidado do dia.
    3. O sistema exibe a mensagem: *"Relatório exportado com sucesso."*
    4. O sistema retorna ao Passo 6 do Fluxo Principal.

#### Fluxos de Exceção (FE)
* **FE01 - Nenhuma Venda Registrada no Dia**
    * *Ponto de Origem:* Passo 2 do Fluxo Principal.
    1. O sistema verifica que nenhuma venda foi realizada na data atual.
    2. O sistema exibe a mensagem: *"Não foram registradas vendas na data de hoje."*
    3. O sistema apresenta o relatório zerado e o caso de uso é encerrado.

---

## 6. Regras de Negócio (RN)

* **RN01 - Disponibilidade de Estoque:** A adição de qualquer produto ao carrinho só pode ser confirmada se a quantidade requisitada for menor ou igual à quantidade do produto disponível em estoque físico/lógico no momento da inserção. A reserva temporária não é mantida até a finalização do pagamento; a baixa definitiva ocorre na aprovação da venda.
* **RN02 - Integração com Gateway Pix:** As cobranças via Pix dependem obrigatoriamente da confirmação assíncrona/síncrona do Gateway de Pagamento integrado ao sistema. Nenhuma venda via Pix pode ser finalizada sem o status de confirmação enviado pelo Gateway.
* **RN03 - Baixa Automática de Estoque:** A atualização do saldo de estoque deve ser realizada imediatamente após a confirmação do pagamento da venda (seja por confirmação do Gateway para Pix ou validação de valor no pagamento em Dinheiro), operando como uma transação atômica (ACID).
* **RN04 - Valor Mínimo para Pagamento em Dinheiro:** Quando a opção de pagamento em Dinheiro for selecionada, o valor total entregue pelo cliente deve ser obrigatoriamente maior ou igual ao valor total da compra.
* **RN05 - Cálculo do Troco:** O troco a ser devolvido ao cliente no pagamento em dinheiro deve ser determinado de acordo com a fórmula: $\text{Troco} = \text{Valor Pago} - \text{Valor Total}$.
* **RN06 - Regra do Alerta de Estoque Baixo:** Produtos com quantidade em estoque menor ou igual a 5 unidades devem receber destaque/alerta visual no momento da consulta.
* **RN07 - Consolidação Diária de Vendas:** O relatório do dia deve considerar apenas vendas concluídas dentro do período de 00:00:00 até 23:59:59 da data em questão.

---

## 7. Dicionário de Dados

### Tabela: Produto
*Armazena as informações do catálogo de picolés e sorvetes de massa disponíveis no carrinho.*

| Entidade / Tabela | Campo / Atributo | Tipo de Dado | Tamanho | Obrigatório | Chave / Índice | Descrição |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Produto** | id | Texto / UUID | 36 | Sim | PK (Primary Key) | Identificador único do produto no sistema. |
| **Produto** | nomeSabor | Texto | 50 | Sim | Único | Nome ou sabor do produto (ex: "Chocolate", "Morango"). |
| **Produto** | tipo | Texto / Enum | 10 | Sim | - | Categoria do produto (PICOLE ou MASSA). |
| **Produto** | precoUnitario | Decimal | (10, 2) | Sim | - | Valor de venda por unidade em R\$. Deve ser $> 0$. |
| **Produto** | quantidadeEstoque | Inteiro | - | Sim | - | Saldo atual disponível no carrinho. Deve ser $\ge 0$. |

### Tabela: Venda
*Registra os dados de cabeçalho de cada transação efetuada.*

| Entidade / Tabela | Campo / Atributo | Tipo de Dado | Tamanho | Obrigatório | Chave / Índice | Descrição |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Venda** | id | Texto / UUID | 36 | Sim | PK (Primary Key) | Identificador único da transação de venda. |
| **Venda** | dataHora | Data/Hora | - | Sim | Índice | Data e hora exata do registro da venda. |
| **Venda** | formaPagamento | Texto / Enum | 10 | Sim | - | Forma de pagamento utilizada (DINHEIRO ou PIX). |
| **Venda** | valorTotal | Decimal | (10, 2) | Sim | - | Somatório do subtotal de todos os itens da venda em R\$. |
| **Venda** | valorPago | Decimal | (10, 2) | Sim | - | Valor recebido do cliente (obrigatório para DINHEIRO). |
| **Venda** | troco | Decimal | (10, 2) | Não | - | Troco devolvido ao cliente ($\text{valorPago} - \text{valorTotal}$). |
| **Venda** | status | Texto / Enum | 10 | Sim | - | Situação da venda (PENDENTE, CONCLUIDA, CANCELADA). |

### Tabela: ItemVenda
*Representa os itens individuais (linhas do pedido) associados a uma venda específica.*

| Entidade / Tabela | Campo / Atributo | Tipo de Dado | Tamanho | Obrigatório | Chave / Índice | Descrição |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **ItemVenda** | id | Texto / UUID | 36 | Sim | PK (Primary Key) | Identificador único do item da venda. |
| **ItemVenda** | vendaId | Texto / UUID | 36 | Sim | FK (Foreign Key) | Referência à venda. |
| **ItemVenda** | produtoId | Texto / UUID | 36 | Sim | FK (Foreign Key) | Referência ao produto vendido. |
| **ItemVenda** | quantidade | Inteiro | - | Sim | - | Quantidade vendida do produto. Deve ser $> 0$. |
| **ItemVenda** | precoUnitarioMomento | Decimal | (10, 2) | Sim | - | Preço praticado no momento da venda. |