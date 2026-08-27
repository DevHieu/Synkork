package com.synkork.backend.modules.payment.service;


import com.synkork.backend.modules.payment.entity.InvoiceEntity;
import com.synkork.backend.modules.payment.entity.PlanPricingEntity;
import com.synkork.backend.modules.payment.enums.BillingCycleEnum;
import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;
import com.synkork.backend.modules.payment.repository.PlanPricingRepository;
import com.synkork.backend.modules.payment.utils.PaymentUtils;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserService;
import com.synkork.backend.modules.user.enums.PlanEnum;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.synkork.backend.modules.payment.utils.PaymentEmail;
import com.synkork.backend.modules.payment.entity.UserSubscriptionEntity;
import com.synkork.backend.modules.payment.enums.*;
import com.synkork.backend.modules.payment.repository.InvoiceRepository;
import com.synkork.backend.modules.payment.repository.UserSubscriptionRepository;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
public class VnpayService {

    @Autowired
    private UserService userService;

    @Autowired
    private PaymentEmail paymentEmail;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ExpiredSubscriptionService expiredSubscriptionService;

    @Autowired
    private PlanPricingRepository planPricingRepository;

    @Autowired
    private PaymentService paymentService;

    @Value("${vnpay.tmn-code}")
    private String vnpTmnCode;

    @Value("${vnpay.hash-secret}")
    private String vnpHashSecret;

    @Value("${vnpay.url}")
    private String vnpUrl;

    @Value("${vnpay.return-url}")
    private String vnpReturnUrl;

    /**
     * Tạo URL thanh toán VNPay
     */
    @Transactional
    public Map<String, Object> createVnPayPayment(String plan, String billingCycle, String userEmail, String clientIp) {
        try {
            PlanEnum planEnum = PlanEnum.valueOf(plan.toUpperCase());
            BillingCycleEnum cycleEnum = BillingCycleEnum.valueOf(billingCycle.toUpperCase());

            PlanPricingEntity pricing = planPricingRepository
                    .findByPlanAndBillingCycleAndActiveTrue(planEnum, cycleEnum)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy giá cho gói này"));

            // Tính giá sau giảm
            BigDecimal finalAmount = PaymentUtils.calculateFinalAmount(pricing);
            BigDecimal discountAmount = pricing.getDiscountAmount() != null
                    ? pricing.getDiscountAmount()
                    : BigDecimal.ZERO;

            UserEntity user = userService.findByEmail(userEmail);

            InvoiceEntity invoice = InvoiceEntity.builder()
                    .user(user)
                    .amount(finalAmount)
                    .plan(planEnum)
                    .billingCycle(cycleEnum)
                    .status(InvoiceStatusEnum.PENDING)
                    .paymentMethod(PaymentMethodEnum.VNPAY)
                    .discountAmount(discountAmount) // lưu số tiền giảm
                    .build();

            invoice = invoiceRepository.save(invoice);
            String orderId = invoice.getId().toString();

            // lưu plan|billing|email vào orderInfo để đọc lại lúc callback
            String orderInfo = plan + "|" + billingCycle + "|" + userEmail;

            long amountVnpay = finalAmount.longValue() * 100; // VNPay tính theo đơn vị x100

            String createDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            Map<String, String> params = new HashMap<>();
            params.put("vnp_Version", "2.1.0");
            params.put("vnp_Command", "pay");
            params.put("vnp_TmnCode", vnpTmnCode);
            params.put("vnp_Amount", String.valueOf(amountVnpay));
            params.put("vnp_CurrCode", "VND");
            params.put("vnp_TxnRef", orderId);
            params.put("vnp_OrderInfo", orderInfo);
            params.put("vnp_OrderType", "other");
            params.put("vnp_Locale", "vn");
            params.put("vnp_ReturnUrl", vnpReturnUrl);
            params.put("vnp_IpAddr", clientIp);
            params.put("vnp_CreateDate", createDate);

            String paymentUrl = buildVnPayUrl(params);

            Map<String, Object> result = new HashMap<>();
            result.put("paymentUrl", paymentUrl);
            result.put("orderId", orderId);
            return result;

        } catch (Exception e) {
            log.error("Lỗi tạo thanh toán VNPay", e);
            throw new RuntimeException("Lỗi tạo thanh toán VNPay: " + e.getMessage());
        }
    }

    @Transactional
    public boolean handleVnPayReturn(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        Map<String, String> fields = new HashMap<>(params);
        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        try {
            if (!isValidVnPaySignature(fields, receivedHash)) {
                log.warn("Invalid VNPay signature — rejected. orderId={}", params.get("vnp_TxnRef"));
                return false;
            }
        } catch (Exception e) {
            log.error("Lỗi verify chữ ký VNPay", e);
            return false;
        }

        String orderId = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");

        InvoiceEntity invoice = invoiceRepository.findById(UUID.fromString(orderId))
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + orderId));

        if (!"00".equals(responseCode)) {
            markInvoiceFailed(invoice, params);
            return false;
        }

        String orderInfo = params.get("vnp_OrderInfo");
        String[] parts = orderInfo.split("\\|", 3);
        String plan = parts[0];
        String billing = parts[1];
        String userEmail = parts[2];

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = PaymentUtils.resolveExpiresAt(now, billing);
        LocalDateTime expireDate = expiresAt.plusDays(3);

        markInvoicePaid(invoice, params, now);

        UserEntity user = userService.findByEmail(userEmail);

        paymentService.createNewSubscription(user, plan, invoice, now, expireDate);
        paymentService.updateUserPlanCache(user, plan, expireDate);

        expiredSubscriptionService.changePendingRoomAndSpace(user.getId());
        paymentEmail.sendPaymentSuccessEmail(userEmail, plan);

        return true;
    }

    private void markInvoiceFailed(InvoiceEntity invoice, Map<String, String> params) {
        invoice.setStatus(InvoiceStatusEnum.FAILED);
        invoice.setTransactionId(params.get("vnp_TransactionNo"));
        invoiceRepository.save(invoice);
    }

    private void markInvoicePaid(InvoiceEntity invoice, Map<String, String> params, LocalDateTime now) {
        invoice.setStatus(InvoiceStatusEnum.PAID);
        invoice.setTransactionId(params.get("vnp_TransactionNo"));
        invoice.setPaidAt(now);
        invoiceRepository.save(invoice);
    }

    /**
     * Build URL thanh toán kèm chữ ký HMAC-SHA512
     */
    private String buildVnPayUrl(Map<String, String> params) throws Exception {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII)).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        String secureHash = hmacSHA512(vnpHashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);

        return vnpUrl + "?" + query;
    }

    private String hmacSHA512(String key, String data) throws Exception {
        javax.crypto.Mac hmac512 = javax.crypto.Mac.getInstance("HmacSHA512");
        javax.crypto.spec.SecretKeySpec secretKeySpec =
                new javax.crypto.spec.SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmac512.init(secretKeySpec);
        byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private boolean isValidVnPaySignature(Map<String, String> fields, String receivedHash) throws Exception {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) hashData.append('&');
            }
        }

        String computedHash = hmacSHA512(vnpHashSecret, hashData.toString());
        return computedHash.equalsIgnoreCase(receivedHash);
    }
}
