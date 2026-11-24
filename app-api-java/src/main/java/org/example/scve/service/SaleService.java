package org.example.scve.service;

import org.example.scve.dto.SaleRequest;
import org.example.scve.dto.SaleResponse;
import org.example.scve.model.PaymentMethod;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SaleService {

    public SaleResponse create(SaleRequest request) {
        return SampleData.sale(UUID.randomUUID(), request.paymentMethod());
    }

    public List<SaleResponse> list() {
        return List.of(
                SampleData.sale(SampleData.SALE_ID, PaymentMethod.CASH),
                SampleData.sale(UUID.fromString("a2b3c4d5-1e2f-4a3b-9c4d-6e7f8a9b0c11"), PaymentMethod.PIX)
        );
    }

    public SaleResponse getById(UUID id) {
        return SampleData.sale(id, PaymentMethod.CASH);
    }
}
