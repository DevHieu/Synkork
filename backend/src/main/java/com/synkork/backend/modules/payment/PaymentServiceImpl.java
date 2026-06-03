package com.synkork.backend.modules.payment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synkork.backend.common.utils.EmailService;
import com.synkork.backend.modules.payment.dto.PaymentResponse;
import com.synkork.backend.modules.user.UserRepository;
import com.synkork.backend.modules.user.enums.PlanEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${momo.partner-code}")
    private String partnerCode;

    @Value("${momo.access-key}")
    private String accessKey;

    @Value("${momo.secret-key}")
    private String secretKey;

    @Value("${momo.endpoint}")
    private String endpoint;

    @Value("${momo.redirect-url}")
    private String redirectUrl;

    @Value("${momo.ipn-url}")
    private String ipnUrl;

    @Override
    public PaymentResponse createMomoPayment(String plan, String userEmail) {
        try {
            long amount = switch (plan.toUpperCase()) {
                case "TEAM"     -> 99000;
                case "BUSINESS" -> 199000;
                default         -> throw new RuntimeException("Plan không hợp lệ");
            };

            String orderId   = UUID.randomUUID().toString();
            String requestId = UUID.randomUUID().toString();

            String extraData = Base64.getEncoder().encodeToString(
                (plan + "|" + userEmail).getBytes()
            );

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
                "&requestType=captureWallet";

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
            body.put("requestType",  "captureWallet");
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

            System.out.println("========== MOMO RESPONSE ==========");
            System.out.println(response.body());

            Map<String, Object> responseMap = mapper.readValue(
                response.body(), new TypeReference<Map<String, Object>>() {}
            );

            Object payUrl = responseMap.get("payUrl");
            if (payUrl == null) throw new RuntimeException("Không tạo được link thanh toán MoMo");

            return PaymentResponse.builder().paymentUrl(payUrl.toString()).build();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi tạo thanh toán MoMo: " + e.getMessage());
        }
    }

    @Override
    public void handleMomoCallback(Map<String, Object> payload) {
        try {
            String resultCode = payload.get("resultCode").toString();
            if (!"0".equals(resultCode)) return;

            String decoded   = new String(Base64.getDecoder().decode(payload.get("extraData").toString()));
            String[] parts   = decoded.split("\\|", 2);
            String plan      = parts[0];
            String userEmail = parts[1];

            System.out.println("✅ Payment success — plan: " + plan + " | email: " + userEmail);

            // ✅ chữ thường
            boolean exists = userRepository.existsByEmail(userEmail);
            System.out.println("🔍 User exists: " + exists + " | email: [" + userEmail + "]");

            userRepository.findByEmail(userEmail).ifPresent(user -> {
                System.out.println("🔄 Updating plan: " + user.getCurrentPlan() + " → " + plan);
                user.setCurrentPlan(PlanEnum.valueOf(plan.toUpperCase()));
                userRepository.save(user);
                System.out.println("✅ Updated plan for " + userEmail + " → " + plan);
            });

            emailService.sendPaymentSuccessEmail(userEmail, plan);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}