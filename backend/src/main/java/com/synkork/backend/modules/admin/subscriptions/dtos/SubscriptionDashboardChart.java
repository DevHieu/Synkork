package com.synkork.backend.modules.admin.subscriptions.dtos;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionDashboardChart {
    private long teamSubscriptions;
    private long businessSubscriptions;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;
}
