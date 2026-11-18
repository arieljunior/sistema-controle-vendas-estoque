# Tutorial — acessar o banco SQLite com o DBeaver

Este tutorial mostra como abrir o banco `scve.db` da aplicação web de vendas no [DBeaver Community](https://dbeaver.io/download/), para consultar produtos, vendas e itens de venda.

## 1. Pré-requisitos

- DBeaver instalado (a edição Community é suficiente).
- Aplicação executada **ao menos uma vez**. O arquivo do banco só é criado na primeira execução, quando as migrations são aplicadas:

  ```powershell
  dotnet run --project .\app-controle-vendas-csharp --launch-profile http
  ```

- Localização do arquivo, a partir da pasta `app-web-csharp`:

  ```text
  app-controle-vendas-csharp\Data\scve.db
  ```

  Junto dele podem aparecer `scve.db-wal` e `scve.db-shm`. São arquivos temporários do SQLite (modo WAL); não os apague enquanto a aplicação estiver rodando.

## 2. Criar a conexão

1. No DBeaver, clique em **Nova Conexão** (ícone de tomada com `+`) ou use o menu **Banco de Dados → Nova Conexão**.
2. Selecione **SQLite** e clique em **Avançar**.
3. Em **Path**, clique em **Browse...** e selecione o arquivo `scve.db`. Ou cole o caminho completo, por exemplo:

   ```text
   C:\...\sistema-controle-vendas-estoque\app-web-csharp\app-controle-vendas-csharp\Data\scve.db
   ```

4. Se aparecer um aviso de **driver não encontrado**, clique em **Download**. O DBeaver baixa o driver JDBC do SQLite automaticamente (é necessário internet apenas nesta etapa).
5. Clique em **Testar Conexão**. Deve aparecer *Connected*.
6. Clique em **Concluir**.

> SQLite não usa usuário nem senha; deixe esses campos em branco.

## 3. Navegar pelo banco

No painel **Navegador de Banco de Dados**, expanda:

```text
scve > Tabelas
```

| Tabela | Conteúdo |
| :--- | :--- |
| `Produto` | Catálogo: `id`, `nomeSabor`, `tipo`, `precoUnitario`, `quantidadeEstoque` |
| `Venda` | Cabeçalho da venda: `id`, `dataHora`, `formaPagamento`, `valorTotal`, `valorPago`, `troco`, `status` |
| `ItemVenda` | Linhas da venda: `id`, `vendaId`, `produtoId`, `quantidade`, `precoUnitarioMomento` |
| `__EFMigrationsHistory` | Controle interno das migrations do EF Core (não altere) |

- **Ver os dados:** clique duas vezes na tabela e abra a aba **Dados**.
- **Ver a estrutura:** na aba **Propriedades**, veja colunas, chaves e índices; na aba **Diagrama ER**, os relacionamentos `ItemVenda → Venda` e `ItemVenda → Produto`.

## 4. Executar consultas SQL

Abra um editor com **SQL Editor → Novo script SQL** (atalho `Ctrl + ]`) e execute com `Ctrl + Enter`.

### Estoque atual, destacando estoque baixo (RN06)

```sql
SELECT nomeSabor, tipo, precoUnitario, quantidadeEstoque,
       CASE WHEN quantidadeEstoque <= 5 THEN 'ESTOQUE BAIXO' ELSE 'OK' END AS situacao
FROM Produto
ORDER BY nomeSabor;
```

### Vendas com seus itens

```sql
SELECT v.dataHora, v.formaPagamento, v.status,
       p.nomeSabor, i.quantidade, i.precoUnitarioMomento
FROM Venda v
JOIN ItemVenda i ON i.vendaId = v.id
JOIN Produto p   ON p.id = i.produtoId
ORDER BY v.dataHora DESC;
```

### Fechamento do dia (RF07 e RN07)

```sql
SELECT formaPagamento,
       COUNT(*)                         AS vendas,
       SUM(CAST(valorTotal AS REAL))    AS faturamento
FROM Venda
WHERE status = 'CONCLUIDA'
  AND date(dataHora) = date('now', 'localtime')
GROUP BY formaPagamento;
```

### Produtos mais vendidos (RF08)

```sql
SELECT p.nomeSabor, SUM(i.quantidade) AS total_vendido
FROM ItemVenda i
JOIN Produto p ON p.id = i.produtoId
JOIN Venda v   ON v.id = i.vendaId
WHERE v.status = 'CONCLUIDA'
GROUP BY p.nomeSabor
ORDER BY total_vendido DESC;
```

## 5. Particularidades do SQLite com EF Core

- **`decimal` é gravado como `TEXT`.** Para somar ou comparar valores monetários no SQL, use `CAST(coluna AS REAL)`, como no exemplo do fechamento do dia.
- **`Guid` é gravado como `TEXT` em maiúsculas** (por exemplo, `1B6F0C1E-3A5D-4C7E-9D11-0A1B2C3D4E01`). Ao filtrar por `id`, use `UPPER('...')` ou copie o valor da própria tela do DBeaver.
- **Datas são `TEXT`** no formato `AAAA-MM-DD HH:MM:SS`; funções como `date()` e `strftime()` funcionam normalmente.
- **Enums são texto:** `PICOLE`/`MASSA`, `DINHEIRO`/`PIX` e `PENDENTE`/`CONCLUIDA`/`CANCELADA`.

## 6. Editando dados pelo DBeaver (com cuidado)

O DBeaver permite editar células direto na aba **Dados**. Em seguida, salve com **Salvar** (`Ctrl + S`) ou com o botão **Commit**, se o modo de transação estiver manual.

Recomendações:

- Use a edição manual só para estudo ou correção pontual (por exemplo, ajustar `quantidadeEstoque`).
- As restrições do banco continuam valendo: preço `> 0`, estoque `>= 0`, quantidade `> 0`, `nomeSabor` único e chaves estrangeiras.
- **Não edite nem apague** `__EFMigrationsHistory`; isso quebra o controle de migrations.
- Alterações de estrutura (colunas, tabelas) devem ser feitas por **migration** do EF Core, não pelo DBeaver. Consulte [onboarding.md](./onboarding.md).

## 7. Problemas comuns

| Sintoma | Causa e solução |
| :--- | :--- |
| `database is locked` ao gravar no DBeaver | A aplicação ou outra conexão está escrevendo. Repita a operação, faça **Commit/Rollback** de transações abertas ou pare a aplicação. |
| Arquivo `scve.db` não existe | A aplicação ainda não foi executada. Rode-a uma vez (item 1). |
| Os dados não refletem uma venda recém-feita | Atualize a aba de dados (`F5`) ou rode a consulta novamente. |
| Quero restaurar os dados iniciais | **Desconecte** no DBeaver (clique direito → *Desconectar*), pare a aplicação e apague `Data/scve.db*`. Na próxima execução o banco é recriado com os produtos de exemplo. |
| O DBeaver mantém o arquivo aberto e impede apagá-lo no Windows | Feche a conexão (*Desconectar*) antes de excluir o arquivo. |
