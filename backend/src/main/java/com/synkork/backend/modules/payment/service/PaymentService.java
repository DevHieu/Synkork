package com.synkork.backend.modules.payment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.payment.entity.InvoiceEntity;
import com.synkork.backend.modules.payment.entity.PlanPricingEntity;
import com.synkork.backend.modules.payment.entity.UserSubscriptionEntity;
import com.synkork.backend.modules.payment.enums.BillingCycleEnum;
import com.synkork.backend.modules.payment.enums.DiscountTypeEnum;
import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;
import com.synkork.backend.modules.payment.enums.PaymentMethodEnum;
import com.synkork.backend.modules.payment.enums.SubscriptionStatusEnum;
import com.synkork.backend.modules.payment.repository.InvoiceRepository;
import com.synkork.backend.modules.payment.repository.PlanPricingRepository;
import com.synkork.backend.modules.payment.repository.UserSubscriptionRepository;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserService;
import com.synkork.backend.modules.user.enums.PlanEnum;
import jakarta.transaction.Transactional;
import org.apache.commons.codec.digest.HmacUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    @Autowired
    private ExpiredSubscriptionService expiredSubscriptionService;

    @Autowired
    private PlanPricingRepository planPricingRepository;

    @Value("${momo.partner-code:}")
    private String partnerCode;

    @Value("${momo.access-key:}")
    private String accessKey;

    @Value("${momo.secret-key:}")
    private String secretKey;

    @Value("${momo.endpoint:}")
    private String endpoint;

    @Value("${momo.redirect-url:}")
    private String redirectUrl;

    @Value("${momo.ipn-url:}")
    private String ipnUrl;

    /**
     * Tạo thanh toán MoMo - Đã tích hợp giảm giá từ PlanPricingEntity
     */
    @Transactional
    public Map<String, Object> createMomoPayment(String plan, String billingCycle, String userEmail) {
        try {
            PlanEnum planEnum = PlanEnum.valueOf(plan.toUpperCase());
            BillingCycleEnum cycleEnum = BillingCycleEnum.valueOf(billingCycle.toUpperCase());

            PlanPricingEntity pricing = planPricingRepository
                    .findByPlanAndBillingCycleAndActiveTrue(planEnum, cycleEnum)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy giá cho gói này"));

            // Tính giá sau giảm
            BigDecimal finalAmount = calculateFinalAmount(pricing);
            BigDecimal discountAmount = pricing.getDiscountAmount() != null 
                    ? pricing.getDiscountAmount() 
                    : BigDecimal.ZERO;

            String requestId = UUID.randomUUID().toString();
            String extraData = Base64.getEncoder().encodeToString(
                    (plan + "|" + billingCycle + "|" + userEmail).getBytes()
            );

            UserEntity user = userService.findByEmail(userEmail);

            InvoiceEntity invoice = InvoiceEntity.builder()
                    .user(user)
                    .amount(finalAmount)
                    .status(InvoiceStatusEnum.PENDING)
                    .paymentMethod(PaymentMethodEnum.MOMO)
                    .discountAmount(discountAmount)   // lưu số tiền giảm
                    .build();

            invoice = invoiceRepository.save(invoice);

            long amountLong = finalAmount.longValue();
            String orderId = invoice.getId().toString();

            String signature = buildRequestSignature(orderId, amountLong, extraData, requestId);

            Map<String, Object> body = buildMomoRequestBody(orderId, amountLong, requestId, extraData, signature);

            ObjectMapper mapper = new ObjectMapper();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            return mapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});

        } catch (Exception e) {
            log.error("Lỗi tạo thanh toán MoMo", e);
            throw new RuntimeException("Lỗi tạo thanh toán MoMo: " + e.getMessage());
        }
    }

    /**
     * Tính giá cuối cùng sau khi áp dụng giảm giá
     */
    private BigDecimal calculateFinalAmount(PlanPricingEntity pricing) {
        BigDecimal baseAmount = pricing.getAmount();
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (pricing.getDiscountType() != null && pricing.getDiscountValue() != null) {
            if (pricing.getDiscountType() == DiscountTypeEnum.PERCENTAGE) {
                discountAmount = baseAmount
                        .multiply(pricing.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 0, BigDecimal.ROUND_HALF_UP);
            } else if (pricing.getDiscountType() == DiscountTypeEnum.FIXED) {
                discountAmount = pricing.getDiscountValue();
            }

            // Không cho giảm vượt quá giá gốc
            discountAmount = discountAmount.min(baseAmount);
        }

        // Cập nhật lại discountAmount vào entity (nếu cần)
        if (pricing.getDiscountAmount() == null || !pricing.getDiscountAmount().equals(discountAmount)) {
            pricing.setDiscountAmount(discountAmount);
            // Không save ở đây vì chỉ là tính toán tạm
        }

        return baseAmount.subtract(discountAmount).max(BigDecimal.ZERO);
    }

    private String buildRequestSignature(String orderId, long amount, String extraData, String requestId) {
        String rawHash =
                "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + orderId +
                "&orderInfo=Synkork VIP" +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=payWithATM";

        return new HmacUtils("HmacSHA256", secretKey).hmacHex(rawHash);
    }

    private Map<String, Object> buildMomoRequestBody(String orderId, long amount, String requestId,
                                                     String extraData, String signature) {
        Map<String, Object> body = new HashMap<>();
        body.put("partnerCode", partnerCode);
        body.put("partnerName", "Synkork");
        body.put("storeId", "Synkork");
        body.put("requestId", requestId);
        body.put("amount", String.valueOf(amount));
        body.put("orderId", orderId);
        body.put("orderInfo", "Synkork VIP");
        body.put("redirectUrl", redirectUrl);
        body.put("ipnUrl", ipnUrl);
        body.put("lang", "vi");
        body.put("extraData", extraData);
        body.put("requestType", "payWithATM");
        body.put("autoCapture", true);
        body.put("signature", signature);
        return body;
    }

    @Transactional
    public void handleMomoCallback(Map<String, Object> payload) {
        try {
            if (!isValidSignature(payload)) {
                log.warn("Invalid MoMo signature — rejected. orderId={}", payload.get("orderId"));
                return;
            }

            String orderId = payload.get("orderId").toString();
            String resultCode = payload.get("resultCode").toString();

            InvoiceEntity invoice = invoiceRepository.findById(UUID.fromString(orderId))
                    .orElseThrow(() -> new RuntimeException("Invoice not found: " + orderId));

            if (!"0".equals(resultCode)) {
                markInvoiceFailed(invoice, payload);
                return;
            }

            String[] parts = decodeExtraData(payload);
            String plan = parts[0];
            String billing = parts[1];
            String userEmail = parts[2];

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiresAt = resolveExpiresAt(now, billing);
            LocalDateTime expireDate = expiresAt.plusDays(3); // buffer 3 ngày

            markInvoicePaid(invoice, payload, now);

            UserEntity user = userService.findByEmail(userEmail);

            deactivateCurrentSubscription(user);
            createNewSubscription(user, plan, invoice, now, expireDate);
            updateUserPlanCache(user, plan, expireDate);

            expiredSubscriptionService.changePendingRoomAndSpace(user.getId());
            emailService.sendPaymentSuccessEmail(userEmail, plan);

        } catch (Exception e) {
            log.error("Lỗi xử lý MoMo callback, orderId={}", payload.get("orderId"), e);
        }
    }
    
    private boolean isValidSignature(Map<String, Object> payload) {
        String rawHash =
                "accessKey=" + accessKey +
                "&amount=" + payload.get("amount") +
                "&extraData=" + payload.get("extraData") +
                "&message=" + payload.get("message") +
                "&orderId=" + payload.get("orderId") +
                "&orderInfo=" + payload.get("orderInfo") +
                "&orderType=" + payload.get("orderType") +
                "&partnerCode=" + partnerCode +
                "&payType=" + payload.get("payType") +
                "&requestId=" + payload.get("requestId") +
                "&responseTime=" + payload.get("responseTime") +
                "&resultCode=" + payload.get("resultCode") +
                "&transId=" + payload.get("transId");

        String expectedSig = new HmacUtils("HmacSHA256", secretKey).hmacHex(rawHash);
        String receivedSig = payload.get("signature").toString();
        return expectedSig.equals(receivedSig);
    }

    private void markInvoiceFailed(InvoiceEntity invoice, Map<String, Object> payload) {
        invoice.setStatus(InvoiceStatusEnum.FAILED);
        invoice.setTransactionId(payload.get("transId") != null ? payload.get("transId").toString() : null);
        invoiceRepository.save(invoice);
    }

    private void markInvoicePaid(InvoiceEntity invoice, Map<String, Object> payload, LocalDateTime now) {
        invoice.setStatus(InvoiceStatusEnum.PAID);
        invoice.setTransactionId(payload.get("transId") != null ? payload.get("transId").toString() : null);
        invoice.setPaidAt(now);
        invoiceRepository.save(invoice);
    }

    private String[] decodeExtraData(Map<String, Object> payload) {
        String extraData = payload.get("extraData").toString();
        String decoded = new String(Base64.getDecoder().decode(extraData));
        return decoded.split("\\|", 3);
    }

    private LocalDateTime resolveExpiresAt(LocalDateTime now, String billing) {
        BillingCycleEnum cycle = BillingCycleEnum.valueOf(billing.toUpperCase());
        return cycle == BillingCycleEnum.YEARLY ? now.plusYears(1) : now.plusMonths(1);
    }

    private void deactivateCurrentSubscription(UserEntity user) {
        userSubscriptionRepository.findByUserIdAndCurrentTrue(user.getId())
                .ifPresent(oldSubscription -> {
                    oldSubscription.setCurrent(false);
                    oldSubscription.setStatus(SubscriptionStatusEnum.EXPIRED);
                    userSubscriptionRepository.save(oldSubscription);
                });
    }

    private void createNewSubscription(UserEntity user, String plan, InvoiceEntity invoice,
                                       LocalDateTime now, LocalDateTime expireDate) {
        UserSubscriptionEntity subscription = UserSubscriptionEntity.builder()
                .user(user)
                .plan(PlanEnum.valueOf(plan.toUpperCase()))
                .status(SubscriptionStatusEnum.ACTIVE)
                .startedAt(now)
                .expiresAt(expireDate)
                .autoRenew(false)
                .invoice(invoice)
                .current(true)
                .build();

        userSubscriptionRepository.save(subscription);
    }

    private void updateUserPlanCache(UserEntity user, String plan, LocalDateTime expireDate) {
        user.setCurrentPlan(PlanEnum.valueOf(plan.toUpperCase()));
        user.setPlanExpiresAt(expireDate);
        userService.create(user);
    }
}