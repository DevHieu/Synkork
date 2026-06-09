package com.synkork.backend.modules.payment;

import com.synkork.backend.modules.payment.dto.InvoiceDTO;
import com.synkork.backend.modules.payment.dto.InvoiceRequestDTO;
import com.synkork.backend.modules.payment.dto.InvoiceSearchDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/invoices")
@RequiredArgsConstructor
public class InvoiceAdminController {

    private final InvoiceAdminService invoiceAdminService;

    @GetMapping
    public ResponseEntity<Page<InvoiceDTO>> getInvoices(InvoiceSearchDTO searchDTO) {
        return ResponseEntity.ok(invoiceAdminService.getInvoices(searchDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDTO> getInvoiceById(@PathVariable UUID id) {
        return ResponseEntity.ok(invoiceAdminService.getInvoiceById(id));
    }

    @PostMapping
    public ResponseEntity<InvoiceDTO> createInvoice(@RequestBody InvoiceRequestDTO request) {
        return ResponseEntity.ok(invoiceAdminService.createInvoice(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceDTO> updateInvoice(@PathVariable UUID id, @RequestBody InvoiceRequestDTO request) {
        return ResponseEntity.ok(invoiceAdminService.updateInvoice(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable UUID id) {
        invoiceAdminService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }
}
