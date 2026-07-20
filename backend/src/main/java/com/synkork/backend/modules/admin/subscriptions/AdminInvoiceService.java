package com.synkork.backend.modules.admin.subscriptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synkork.backend.common.utils.AuthUtils;
import com.synkork.backend.modules.admin.auditLog.AuditLogService;
import com.synkork.backend.modules.admin.auditLog.dtos.BuildLog;
import com.synkork.backend.modules.admin.auditLog.enums.LogActionEnum;
import com.synkork.backend.modules.admin.auditLog.enums.LogEntityTypeEnum;
import com.synkork.backend.modules.admin.subscriptions.dtos.AdminInvoiceRequest;
import com.synkork.backend.modules.admin.subscriptions.dtos.AdminInvoiceUpdateRequest;
import com.synkork.backend.modules.admin.subscriptions.dtos.AdminInvoiceResponse;
import com.synkork.backend.modules.admin.subscriptions.dtos.InvoiceFilterRequest;
import com.synkork.backend.modules.payment.entity.InvoiceEntity;
import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;
import com.synkork.backend.modules.payment.repository.InvoiceRepository;
import com.synkork.backend.modules.payment.service.ExpiredSubscriptionService;
import com.synkork.backend.modules.report.enums.ReportStatusEnums;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.modules.user.enums.PlanEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminInvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final ExpiredSubscriptionService expiredSubscriptionService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public Page<InvoiceEntity> getInvoices(InvoiceFilterRequest request) {
        request.validate();

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Specification<InvoiceEntity> specification = InvoiceSpecification.filter(request);

        return invoiceRepository.findAll(specification, pageable);
    }

    public AdminInvoiceResponse getInvoiceById(UUID id) {
        return AdminInvoiceResponse.from(findOrThrow(id));
    }

    @Transactional
    public AdminInvoiceResponse createInvoice(AdminInvoiceRequest request) {
        UserEntity user = userRepository.findByEmail(request.userEmail())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với email: " + request.userEmail()));
        
        PlanEnum targetPlan = request.plan() != null ? request.plan() : user.getCurrentPlan();

        InvoiceEntity invoice = InvoiceEntity.builder()
                .user(user)
                .amount(request.amount())
                .status(request.status() != null ? request.status() : InvoiceStatusEnum.PENDING)
                .paymentMethod(request.paymentMethod())
                .transactionId(request.orderId())
                .paidAt(request.status() == InvoiceStatusEnum.PAID ? LocalDateTime.now() : null)
                .build();
        
        InvoiceEntity saved = invoiceRepository.save(invoice);

        if (request.status() == InvoiceStatusEnum.PAID) {
            updateUserPlan(user, targetPlan);
        }

        createLog(saved, LogActionEnum.CREATE_INVOICE, null);

        return AdminInvoiceResponse.from(saved);
    }

    @Transactional
    public AdminInvoiceResponse updateInvoice(UUID id, AdminInvoiceUpdateRequest request) {
        InvoiceEntity invoice = findOrThrow(id);
        InvoiceStatusEnum previousStatus = invoice.getStatus();

        if (request.userEmail() != null) {
            UserEntity user = userRepository.findByEmail(request.userEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với email: " + request.userEmail()));
            invoice.setUser(user);
        }

        if (request.amount() != null) {
            invoice.setAmount(request.amount());
        }

        if (request.status() != null) {
            InvoiceStatusEnum newStatus = request.status();
            if (newStatus == InvoiceStatusEnum.PAID && invoice.getStatus() != InvoiceStatusEnum.PAID) {
                invoice.setPaidAt(LocalDateTime.now());
                PlanEnum targetPlan = request.plan() != null ? request.plan() : invoice.getUser().getCurrentPlan();
                updateUserPlan(invoice.getUser(), targetPlan);
            }
            invoice.setStatus(newStatus);
        }

        if (request.paymentMethod() != null) {
            invoice.setPaymentMethod(request.paymentMethod());
        }

        if (request.orderId() != null) {
            invoice.setTransactionId(request.orderId());
        }

        InvoiceEntity saved = invoiceRepository.save(invoice);
        createLog(saved, LogActionEnum.UPDATE_INVOICE, previousStatus);
        return AdminInvoiceResponse.from(saved);
    }

    @Transactional
    public void deleteInvoice(UUID id) {
        InvoiceEntity invoice = findOrThrow(id);
        createLog(invoice, LogActionEnum.DELETE_INVOICE, invoice.getStatus());
        invoiceRepository.delete(invoice);
    }

    private InvoiceEntity findOrThrow(UUID id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn: " + id));
    }

    private void updateUserPlan(UserEntity user, PlanEnum plan) {
        user.setCurrentPlan(plan);
        user.setPlanExpiresAt(plan == PlanEnum.FREE ? null : LocalDateTime.now().plusMonths(1).plusDays(3));
        userRepository.save(user);
        try {
            expiredSubscriptionService.changePendingRoomAndSpace(user.getId());
        } catch (Exception e) {
            log.error("Failed to change pending room and space for user: {}", user.getId(), e);
        }
    }

    private void createLog(InvoiceEntity entity, LogActionEnum action, InvoiceStatusEnum previousStatus) {
        BuildLog log = BuildLog.builder()
                .action(action)
                .entityType(LogEntityTypeEnum.SUBSCRIPTION)
                .entityId(entity.getId().toString())
                .entityName(entity.getUser().getEmail()) // dùng email làm name cho dễ đọc trong audit log
                .description(AuthUtils.getCurrentUsername() + " đã thực hiện " + action.name() + " hóa đơn của " + entity.getUser().getEmail())
                .metadata(createInvoiceMetadata(entity, previousStatus))
                .build();

        auditLogService.log(log);
    }

    private String createInvoiceMetadata(InvoiceEntity entity, InvoiceStatusEnum previousStatus) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("invoiceId", entity.getId().toString());
            metadata.put("userEmail", entity.getUser().getEmail());
            metadata.put("amount", entity.getAmount().toString());
            metadata.put("paymentMethod", entity.getPaymentMethod() != null ? entity.getPaymentMethod().name() : null);
            metadata.put("transactionId", entity.getTransactionId());
            metadata.put("previousStatus", previousStatus != null ? previousStatus.name() : null);
            metadata.put("newStatus", entity.getStatus().name());
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize invoice metadata", e);
        }
    }
}
