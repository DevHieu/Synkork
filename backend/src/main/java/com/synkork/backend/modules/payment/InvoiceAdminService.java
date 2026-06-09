package com.synkork.backend.modules.payment;

import com.synkork.backend.modules.payment.dto.InvoiceDTO;
import com.synkork.backend.modules.payment.dto.InvoiceRequestDTO;
import com.synkork.backend.modules.payment.dto.InvoiceSearchDTO;
import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.modules.user.enums.PlanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class InvoiceAdminService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpiredSubscriptionService expiredSubscriptionService;

    public Page<InvoiceDTO> getInvoices(InvoiceSearchDTO searchDTO) {
        Pageable pageable = PageRequest.of(
                Math.max(searchDTO.getPage(), 0),
                Math.max(searchDTO.getSize(), 1),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        
        Specification<InvoiceEntity> specification = Specification.allOf(
                InvoiceSpecifications.fetchUser(),
                InvoiceSpecifications.hasStatus(searchDTO.getStatus()),
                InvoiceSpecifications.hasPlan(searchDTO.getPlan()),
                InvoiceSpecifications.hasPaymentMethod(searchDTO.getPaymentMethod()),
                InvoiceSpecifications.hasEmail(searchDTO.getEmail()),
                InvoiceSpecifications.hasUsername(searchDTO.getUsername()),
                InvoiceSpecifications.createdAtBetween(searchDTO.getStartDate(), searchDTO.getEndDate())
        );

        return invoiceRepository.findAll(specification, pageable).map(InvoiceMapper::toDto);
    }

    public InvoiceDTO getInvoiceById(UUID id) {
        return invoiceRepository.findById(id)
                .map(InvoiceMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn"));
    }

    public InvoiceDTO createInvoice(InvoiceRequestDTO request) {
        UserEntity user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với email: " + request.getUserEmail()));
        PlanEnum targetPlan = request.getPlan() != null ? request.getPlan() : user.getCurrentPlan();

        InvoiceEntity invoice = InvoiceEntity.builder()
                .user(user)
                .amount(request.getAmount())
                .status(request.getStatus() != null ? request.getStatus() : InvoiceStatusEnum.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .transactionId(request.getOrderId())
                .paidAt(request.getStatus() == InvoiceStatusEnum.PAID ? LocalDateTime.now() : null)
                .build();
        invoiceRepository.save(invoice);

        if (request.getStatus() == InvoiceStatusEnum.PAID) {
            updateUserPlan(user, targetPlan);
        }

        return getInvoiceById(invoice.getId());
    }

    public InvoiceDTO updateInvoice(UUID id, InvoiceRequestDTO request) {
        InvoiceEntity invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn"));

        if (request.getUserEmail() != null) {
            String email = request.getUserEmail();
            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với email: " + email));
            invoice.setUser(user);
        }

        if (request.getAmount() != null) {
            invoice.setAmount(request.getAmount());
        }

        if (request.getStatus() != null) {
            InvoiceStatusEnum newStatus = request.getStatus();
            if (newStatus == InvoiceStatusEnum.PAID && invoice.getStatus() != InvoiceStatusEnum.PAID) {
                invoice.setPaidAt(LocalDateTime.now());
                PlanEnum targetPlan = request.getPlan() != null ? request.getPlan() : invoice.getUser().getCurrentPlan();
                updateUserPlan(invoice.getUser(), targetPlan);
            }
            invoice.setStatus(newStatus);
        }

        if (request.getPaymentMethod() != null) {
            invoice.setPaymentMethod(request.getPaymentMethod());
        }

        if (request.getOrderId() != null) {
            invoice.setTransactionId(request.getOrderId());
        }

        invoiceRepository.save(invoice);
        return getInvoiceById(id);
    }

    public void deleteInvoice(UUID id) {
        InvoiceEntity invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn"));
        invoiceRepository.delete(invoice);
    }

    private Specification<InvoiceEntity> and(Specification<InvoiceEntity> left, Specification<InvoiceEntity> right) {
        return left == null ? right : (right == null ? left : left.and(right));
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
