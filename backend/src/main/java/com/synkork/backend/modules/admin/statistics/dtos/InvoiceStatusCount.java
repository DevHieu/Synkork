package com.synkork.backend.modules.admin.statistics.dtos;

import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;

public record InvoiceStatusCount(
    long count,
    InvoiceStatusEnum status
) {
}
