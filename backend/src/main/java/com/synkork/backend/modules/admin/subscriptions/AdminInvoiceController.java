package com.synkork.backend.modules.admin.subscriptions;

import com.synkork.backend.common.response.ApiResponse;
import com.synkork.backend.common.response.PageMeta;
import com.synkork.backend.modules.admin.subscriptions.dtos.AdminInvoiceRequest;
import com.synkork.backend.modules.admin.subscriptions.dtos.AdminInvoiceUpdateRequest;
import com.synkork.backend.modules.admin.subscriptions.dtos.AdminInvoiceResponse;
import com.synkork.backend.modules.admin.subscriptions.dtos.InvoiceFilterRequest;
import com.synkork.backend.modules.payment.entity.InvoiceEntity;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/manage/invoices")
@RequiredArgsConstructor
public class AdminInvoiceController {

    private final AdminInvoiceService adminInvoiceService;

    @GetMapping
    public ApiResponse<List<AdminInvoiceResponse>> getInvoices(@Valid @ModelAttribute InvoiceFilterRequest request) {
        Page<AdminInvoiceResponse> list = adminInvoiceService.getInvoices(request).map(AdminInvoiceResponse::from);

        return ApiResponse.success(
                "Get invoice list successfully",
                list.getContent(),
                PageMeta.from(list)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminInvoiceResponse> getInvoiceById(@PathVariable UUID id) {
        return ApiResponse.success("Get invoice successfully", adminInvoiceService.getInvoiceById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AdminInvoiceResponse> createInvoice(@Valid @RequestBody AdminInvoiceRequest request) {
        return ApiResponse.success(
                "Create invoice successfully",
                adminInvoiceService.createInvoice(request)
        );
    }

    @PatchMapping("/{id}")
    public ApiResponse<AdminInvoiceResponse> updateInvoice(
            @PathVariable UUID id,
            @Valid @RequestBody AdminInvoiceUpdateRequest request
    ) {
        return ApiResponse.success(
                "Update invoice successfully",
                adminInvoiceService.updateInvoice(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteInvoice(@PathVariable UUID id) {
        adminInvoiceService.deleteInvoice(id);
        return ApiResponse.success("Delete invoice successfully", null);
    }
}
