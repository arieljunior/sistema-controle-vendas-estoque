package org.example.scve.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    public static final String BAD_REQUEST = "BadRequest";
    public static final String NOT_FOUND = "NotFound";
    public static final String UNPROCESSABLE = "UnprocessableEntity";

    private static final String PROBLEM_DETAIL = "ProblemDetail";
    private static final String PROBLEM_JSON = "application/problem+json";

    @Bean
    public OpenAPI scveOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SCVE - API de Controle de Vendas e Estoque")
                        .version("v0.1.0")
                        .description("""
                                Versão REST do Sistema de Controle de Vendas e Estoque (SCVE) para o vendedor \
                                ambulante de sorvetes. Cobre cadastro e consulta de produtos, registro de vendas \
                                (Dinheiro/Pix) e relatório de fechamento do dia.""")
                        .contact(new Contact().name("Equipe SCVE")))
                .addTagsItem(new Tag().name("Produtos").description("Cadastro e consulta de estoque"))
                .addTagsItem(new Tag().name("Vendas").description("Registro de vendas no carrinho"))
                .addTagsItem(new Tag().name("Relatórios").description("Fechamento do dia"))
                .components(new Components()
                        .addSchemas(PROBLEM_DETAIL, problemDetailSchema())
                        .addResponses(BAD_REQUEST, problem("Requisição inválida (dados ausentes ou fora do formato)."))
                        .addResponses(NOT_FOUND, problem("Recurso não encontrado."))
                        .addResponses(UNPROCESSABLE, problem("Regra de negócio violada (ex.: estoque insuficiente).")));
    }

    private Schema<?> problemDetailSchema() {
        return new ObjectSchema()
                .description("Erro padronizado (RFC 9457 - Problem Details)")
                .addProperty("type", new StringSchema().example("about:blank"))
                .addProperty("title", new StringSchema().example("Dados inválidos"))
                .addProperty("status", new IntegerSchema().example(400))
                .addProperty("detail", new StringSchema().example("Os dados informados são inválidos."))
                .addProperty("instance", new StringSchema().example("/api/products"))
                .addProperty("errors", new ArraySchema()
                        .items(new StringSchema())
                        .description("Detalhes por campo (presente em erros de validação)")
                        .example(List.of("unitPrice: O preço unitário deve ser maior que zero.")));
    }

    private ApiResponse problem(String description) {
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(PROBLEM_JSON,
                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/" + PROBLEM_DETAIL))));
    }
}
