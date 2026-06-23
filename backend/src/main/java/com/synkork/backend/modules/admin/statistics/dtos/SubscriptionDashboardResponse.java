package com.synkork.backend.modules.admin.statistics.dtos;

import com.synkork.backend.modules.payment.dto.InvoiceDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionDashboardResponse {
    private BigDecimal totalRevenue;
    private BigDecimal revenueThisMonth;
    private long activeSubscriptions;
    private long pendingInvoices;
    private long paidInvoices;
    private long failedInvoices;
    private List<InvoiceDTO> recentTransactions;
}
