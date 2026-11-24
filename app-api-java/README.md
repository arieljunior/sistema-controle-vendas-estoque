# SCVE – API REST (Spring Boot)

## Stack

Java 21 · Spring Boot 4.1 · Maven (wrapper incluso) · Bean Validation · springdoc-openapi (Swagger UI) · Actuator

## Como rodar

Requisito: **JDK 21** (o `JAVA_HOME` precisa apontar para ele).

```powershell
.\mvnw.cmd spring-boot:run      # sobe em http://localhost:8080
.\mvnw.cmd test                 # executa os testes
```

| Recurso | URL |
| --- | --- |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI (JSON) | http://localhost:8080/v3/api-docs |
| Health | http://localhost:8080/actuator/health |

## Endpoints

| Método | Rota | Caso de uso | Resposta |
| --- | --- | --- | --- |
| GET | `/api/products` | UC03 Consultar estoque | 200 |
| GET | `/api/products/{id}` | UC03 | 200 / 404 |
| POST | `/api/products` | UC02 Cadastrar produto | 201 + `Location` / 400 / 422 |
| PUT | `/api/products/{id}` | UC02 | 200 / 400 / 404 |
| DELETE | `/api/products/{id}` | – | 204 / 404 |
| POST | `/api/sales` | UC01 Registrar venda | 201 + `Location` / 400 / 404 / 422 |
| GET | `/api/sales` | – | 200 |
| GET | `/api/sales/{id}` | UC01 (recibo) | 200 / 404 |
| GET | `/api/reports/daily?date=AAAA-MM-DD` | UC04 Fechamento do dia | 200 / 400 |

No REST não há "carrinho em sessão": o carrinho inteiro vai no corpo do `POST /api/sales`, e cancelar a venda equivale a não enviar a requisição.

Valores de enum seguem o dicionário de dados da spec: `PICOLE`/`MASSA`, `DINHEIRO`/`PIX`, `PENDENTE`/`CONCLUIDA`/`CANCELADA`.

### Exemplo – registrar venda em dinheiro

```http
POST /api/sales
Content-Type: application/json

{
  "items": [{ "productId": "1b6f0c1e-3a5d-4c7e-9d11-0a1b2c3d4e01", "quantity": 2 }],
  "paymentMethod": "DINHEIRO",
  "amountPaid": 50.00
}
```

`amountPaid` é obrigatório quando o pagamento é `DINHEIRO`. Erros seguem o padrão **ProblemDetail** (RFC 9457), por exemplo:

```json
{
  "title": "Dados inválidos",
  "status": 400,
  "detail": "Os dados informados são inválidos.",
  "instance": "/api/sales",
  "errors": ["items: O carrinho está vazio. Adicione ao menos um item."]
}
```

## Estrutura (`src/main/java/org/example/scve`)

| Pacote | Responsabilidade |
| --- | --- |
| `controller` | Rotas HTTP e anotações OpenAPI. Sem regra de negócio. |
| `dto` | `record`s de entrada/saída com Bean Validation. |
| `service` | Regras de negócio (hoje: *stubs* com `SampleData`). |
| `model` | Enums do domínio. |
| `config` | Metadados do OpenAPI e respostas de erro reutilizáveis. |
| `exception` | `GlobalExceptionHandler` → `ProblemDetail` em português. |
