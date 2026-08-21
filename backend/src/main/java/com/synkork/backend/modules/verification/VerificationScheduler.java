package com.synkork.backend.modules.verification;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class VerificationScheduler {

    @Autowired
    private VerificationRepository verificationRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteExpiredVerifications() {
        verificationRepository.deleteExpiredVerifications();
        System.out.println("delete verification sql successfully.");
    }
}
