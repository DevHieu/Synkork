package com.synkork.backend.modules.payment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.payment.enums.BillingCycleEnum;
import com.synkork.backend.modules.payment.enums.InvoiceStatusEnum;
import com.synkork.backend.modules.payment.enums.PaymentMethodEnum;
import com.synkork.backend.modules.user.UserEntity;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.modules.user.UserService;
import com.synkork.backend.modules.user.enums.PlanEnum;
import org.apache.commons.codec.digest.HmacUtils;
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

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private InvoiceRepository invoiceRepository;

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
    @Autowired
    private ExpiredSubscriptionService expiredSubscriptionService;

    public Map<String, Object> createMomoPayment(String plan, String billingCycle, String userEmail) {
        try {
            long amount = switch (plan.toUpperCase()) {
                case "TEAM"     -> billingCycle.equalsIgnoreCase("MONTHLY") ? 69000 : 659000;
                case "BUSINESS" -> billingCycle.equalsIgnoreCase("MONTHLY") ? 129000 : 1239000;
                default        -> throw new RuntimeException("Plan không hợp lệ");
            };

            String requestId = UUID.randomUUID().toString();

            String extraData = Base64.getEncoder().encodeToString(
                    (plan + "|" + billingCycle + "|" + userEmail).getBytes()
            );

            // Tạo invoice PENDING trước
            UserEntity user = userService.findByEmail(userEmail);

            InvoiceEntity invoice = InvoiceEntity.builder()
                    .user(user)
                    .amount(new BigDecimal(amount))
                    .status(InvoiceStatusEnum.PENDING)
                    .paymentMethod(PaymentMethodEnum.MOMO)
                    .build();

            invoiceRepository.save(invoice);

            String orderId = invoice.getId().toString(); // dùng invoice id làm orderId

            String rawHash =
                    "accessKey="    + accessKey   +
                            "&amount="      + amount      +
                            "&extraData="   + extraData   +
                            "&ipnUrl="      + ipnUrl      +
                            "&orderId="     + orderId     +
                            "&orderInfo=Synkork VIP"      +
                            "&partnerCode=" + partnerCode +
                            "&redirectUrl=" + redirectUrl +
                            "&requestId="   + requestId   +
                            "&requestType=payWithATM";

            String signature = new HmacUtils("HmacSHA256", secretKey).hmacHex(rawHash);

            Map<String, Object> body = new HashMap<>();
            body.put("partnerCode",  partnerCode);
            body.put("partnerName",  "Synkork");
            body.put("storeId",      "Synkork");
            body.put("requestId",    requestId);
            body.put("amount",       String.valueOf(amount));
            body.put("orderId",      orderId);
            body.put("orderInfo",    "Synkork VIP");
            body.put("redirectUrl",  redirectUrl);
            body.put("ipnUrl",       ipnUrl);
            body.put("lang",         "vi");
            body.put("extraData",    extraData);
            body.put("requestType",  "payWithATM");
            body.put("autoCapture",  true);
            body.put("signature",    signature);

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
            e.printStackTrace();
            throw new RuntimeException("Lỗi tạo thanh toán MoMo: " + e.getMessage());
        }
    }

    public void handleMomoCallback(Map<String, Object> payload) {
        try {
            String rawHash =
                    "accessKey="     + accessKey +
                            "&amount="       + payload.get("amount") +
                            "&extraData="    + payload.get("extraData") +
                            "&message="      + payload.get("message") +
                            "&orderId="      + payload.get("orderId") +
                            "&orderInfo="    + payload.get("orderInfo") +
                            "&orderType="    + payload.get("orderType") +
                            "&partnerCode="  + partnerCode +
                            "&payType="      + payload.get("payType") +
                            "&requestId="    + payload.get("requestId") +
                            "&responseTime=" + payload.get("responseTime") +
                            "&resultCode="   + payload.get("resultCode") +
                            "&transId="      + payload.get("transId");

            String expectedSig = new HmacUtils("HmacSHA256", secretKey).hmacHex(rawHash);
            String receivedSig = payload.get("signature").toString();

            if (!expectedSig.equals(receivedSig)) {
                System.out.println("Invalid MoMo signature — rejected");
                return;
            }

            String orderId    = payload.get("orderId").toString();
            String resultCode = payload.get("resultCode").toString();

            InvoiceEntity invoice = invoiceRepository.findById(UUID.fromString(orderId))
                    .orElseThrow(() -> new RuntimeException("Invoice not found: " + orderId));

            // Thất bại → update FAILED rồi thôi
            if (!"0".equals(resultCode)) {
                invoice.setStatus(InvoiceStatusEnum.FAILED);
                invoice.setTransactionId(payload.get("transId").toString());
                invoiceRepository.save(invoice);
                return;
            }

            String decoded   = new String(Base64.getDecoder().decode(payload.get("extraData").toString()));
            String[] parts   = decoded.split("\\|", 3);
            String plan      = parts[0];
            String billing   = parts[1];
            String userEmail = parts[2];

            LocalDateTime now       = LocalDateTime.now();
            BillingCycleEnum cycle  = BillingCycleEnum.valueOf(billing.toUpperCase());
            LocalDateTime expiresAt = cycle == BillingCycleEnum.YEARLY
                    ? now.plusYears(1)
                    : now.plusMonths(1);

            // Update invoice PAID
            invoice.setStatus(InvoiceStatusEnum.PAID);
            invoice.setTransactionId(payload.get("transId").toString());
            invoice.setPaidAt(now);
            invoiceRepository.save(invoice);

            // Update user
            UserEntity user = userService.findByEmail(userEmail);
            user.setCurrentPlan(PlanEnum.valueOf(plan.toUpperCase()));
            user.setPlanExpiresAt(expiresAt.plusDays(3)); // Cộng thêm 3 ngày để người dùng có thời gian để renew gói
            userService.create(user);
            expiredSubscriptionService.changePendingRoomAndSpace(user.getId());

            emailService.sendPaymentSuccessEmail(userEmail, plan);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}