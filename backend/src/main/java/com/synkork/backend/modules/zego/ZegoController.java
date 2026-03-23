package com.synkork.backend.modules.zego;

import com.synkork.backend.common.utils.zegoCloud.TokenServerAssistant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/zego")
public class ZegoController {

    @Value("${zego.appId}")
    private long appId;

    @Value("${zego.secret}")
    private String serverSecret;

    @GetMapping("/token/{userId}")
    public ResponseEntity<?> getToken(@PathVariable String userId) {
        System.out.println("appId: " + appId );
        System.out.println("serverSecret: " + serverSecret);
        System.out.println("userId BE: " + userId);

        TokenServerAssistant.TokenInfo tokenInfo =
                TokenServerAssistant.generateToken04(
                        appId, userId, serverSecret, 3600, ""
                );

        if (tokenInfo.error.code != TokenServerAssistant.ErrorCode.SUCCESS) {
            return ResponseEntity.badRequest().body(tokenInfo.error.message);
        }

        System.out.println(tokenInfo.data);
        return ResponseEntity.ok(tokenInfo.data);
    }
}