package com.synkork.backend.modules.payment.entity;

import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;
import com.synkork.backend.modules.payment.enums.PaymentMethodEnum;
import com.synkork.backend.modules.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // xai cua lombok cho code gọn
public class InvoiceEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UserEntity user;

    // Số tiền THỰC THU (đã áp khuyến mãi nếu có)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // PENDING | PAID | FAILED | REFUNDED
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatusEnum status;

    // MOMO | VNPAY
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentMethodEnum paymentMethod;

    @Column(length = 255)
    private String transactionId;

    private LocalDateTime paidAt;

    // Mã khuyến mãi đã áp dụng cho hoá đơn này (nếu có), để đối soát/báo cáo sau này
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", columnDefinition = "BINARY(16)")
    private PromotionEntity promotion;

    // Số tiền đã được giảm nhờ khuyến mãi (0 nếu không dùng mã nào)
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}