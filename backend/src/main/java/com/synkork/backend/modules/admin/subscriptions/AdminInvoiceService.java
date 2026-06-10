package com.synkork.backend.modules.admin.subscriptions;

import com.synkork.backend.modules.admin.subscriptions.dtos.AdminInvoiceRequest;
import com.synkork.backend.modules.admin.subscriptions.dtos.AdminInvoiceResponse;
import com.synkork.backend.modules.admin.subscriptions.dtos.InvoiceFilterRequest;
import com.synkork.backend.modules.payment.ExpiredSubscriptionService;
import com.synkork.backend.modules.payment.InvoiceEntity;
import com.synkork.backend.modules.payment.InvoiceRepository;
import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.modules.user.enums.PlanEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminInvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpiredSubscriptionService expiredSubscriptionService;

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

        return AdminInvoiceResponse.from(saved);
    }

    @Transactional
    public AdminInvoiceResponse updateInvoice(UUID id, AdminInvoiceRequest request) {
        InvoiceEntity invoice = findOrThrow(id);

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

        return AdminInvoiceResponse.from(invoiceRepository.save(invoice));
    }

    @Transactional
    public void deleteInvoice(UUID id) {
        invoiceRepository.delete(findOrThrow(id));
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
        } catch (Exception ignored) {
        }
    }
}
