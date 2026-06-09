package com.synkork.backend.modules.payment;

import com.synkork.backend.modules.payment.dto.InvoiceDTO;
import lombok.experimental.UtilityClass;

@UtilityClass
public class InvoiceMapper {

    public static InvoiceDTO toDto(InvoiceEntity invoice) {
        if (invoice == null) {
            return null;
        }
        return InvoiceDTO.builder()
                .id(invoice.getId())
                .amount(invoice.getAmount())
                .paidAt(invoice.getPaidAt())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .plan(invoice.getUser() != null ? invoice.getUser().getCurrentPlan() : null)
                .status(invoice.getStatus())
                .transactionId(invoice.getTransactionId())
                .paymentMethod(invoice.getPaymentMethod())
                .userEmail(invoice.getUser() != null ? invoice.getUser().getEmail() : null)
                .username(invoice.getUser() != null ? invoice.getUser().getUsername() : null)
                .build();
    }
}
