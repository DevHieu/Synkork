package com.synkork.backend.modules.admin.subscriptions.controller;

import com.synkork.backend.common.response.ApiResponse;
import com.synkork.backend.common.response.PageMeta;
import com.synkork.backend.modules.admin.subscriptions.service.AdminSubscriptionService;
import com.synkork.backend.modules.admin.subscriptions.dtos.AdminSubscriptionFilterRequest;
import com.synkork.backend.modules.admin.subscriptions.dtos.AdminSubscriptionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/manage/subscriptions")
@RequiredArgsConstructor
public class AdminSubscriptionController {
    private final AdminSubscriptionService adminSubscriptionService;

    @GetMapping
    public ApiResponse<List<AdminSubscriptionResponse>> getSubscriptions(
            @Valid @ModelAttribute AdminSubscriptionFilterRequest request
    ) {
        Page<AdminSubscriptionResponse> list = adminSubscriptionService.getSubscriptions(request);

        return ApiResponse.success(
                "Get subscription list successfully",
                list.getContent(),
                PageMeta.from(list)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminSubscriptionResponse> getSubscriptionById(@PathVariable UUID id) {
        return ApiResponse.success(
                "Get subscription successfully",
                adminSubscriptionService.getSubscriptionById(id)
        );
    }
}
