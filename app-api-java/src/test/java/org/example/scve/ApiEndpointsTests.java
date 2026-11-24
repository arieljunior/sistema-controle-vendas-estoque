package org.example.scve;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiEndpointsTests {

    private static final String CHOCOLATE_ID = "1b6f0c1e-3a5d-4c7e-9d11-0a1b2c3d4e01";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsProductsWithLowStockFlag() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].type").value("MASSA"))
                .andExpect(jsonPath("$[2].lowStock").value(true));
    }

    @Test
    void createsProductReturningLocation() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"flavorName":"Coco","type":"PICOLE","unitPrice":6.00,"stockQuantity":20}"""))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.flavorName").value("Coco"));
    }

    @Test
    void rejectsInvalidProductWithProblemDetail() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"flavorName":"","type":"PICOLE","unitPrice":0,"stockQuantity":-1}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Dados inválidos"))
                .andExpect(jsonPath("$.errors.length()").value(3));
    }

    @Test
    void rejectsUnknownProductType() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"flavorName":"Coco","type":"SORVETE","unitPrice":6.00,"stockQuantity":1}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Requisição ilegível"));
    }

    @Test
    void deletesProduct() throws Exception {
        mockMvc.perform(delete("/api/products/" + CHOCOLATE_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void rejectsMalformedProductId() throws Exception {
        mockMvc.perform(get("/api/products/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createsCashSale() throws Exception {
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"productId":"%s","quantity":2}],"paymentMethod":"DINHEIRO","amountPaid":50.00}"""
                                .formatted(CHOCOLATE_ID)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.paymentMethod").value("DINHEIRO"))
                .andExpect(jsonPath("$.change").value(20.00));
    }

    @Test
    void createsPixSaleWithoutChange() throws Exception {
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"productId":"%s","quantity":1}],"paymentMethod":"PIX"}"""
                                .formatted(CHOCOLATE_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentMethod").value("PIX"))
                .andExpect(jsonPath("$.change").value(0.00));
    }

    @Test
    void rejectsCashSaleWithoutAmountPaid() throws Exception {
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[{"productId":"%s","quantity":1}],"paymentMethod":"DINHEIRO"}"""
                                .formatted(CHOCOLATE_ID)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsSaleWithEmptyCart() throws Exception {
        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"items":[],"paymentMethod":"PIX"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("items: O carrinho está vazio. Adicione ao menos um item."));
    }

    @Test
    void listsAndGetsSales() throws Exception {
        mockMvc.perform(get("/api/sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/api/sales/9f1e7c52-6a3b-4d0e-8c14-5b2a7d9e3f10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    void returnsDailyReportForGivenDate() throws Exception {
        mockMvc.perform(get("/api/reports/daily").param("date", "2026-11-21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-11-21"))
                .andExpect(jsonPath("$.totalRevenue").value(205.50))
                .andExpect(jsonPath("$.topProducts[0].flavorName").value("Chocolate"));
    }

    @Test
    void rejectsInvalidReportDate() throws Exception {
        mockMvc.perform(get("/api/reports/daily").param("date", "21/11/2026"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exposesOpenApiDocumentation() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("SCVE - API de Controle de Vendas e Estoque"))
                .andExpect(jsonPath("$.paths['/api/products']").exists())
                .andExpect(jsonPath("$.paths['/api/sales']").exists())
                .andExpect(jsonPath("$.paths['/api/reports/daily']").exists())
                .andExpect(jsonPath("$.components.schemas.ProblemDetail.properties.errors").exists())
                .andExpect(jsonPath("$.components.schemas.SaleRequest.properties.paymentMethod.enum[0]").value("DINHEIRO"));
    }
}
