package dev.kolombetm.payflow.kyc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "test"})
public class MockKycProvider implements KycProviderInterface {
    private static final Logger log = LoggerFactory.getLogger(MockKycProvider.class);

    public MockKycProvider() {
        log.warn("MockKycProvider is active — KYC checks are NOT real");
    }

    @Override
    public KycStatus verify(String userId) {
        return KycStatus.VERIFIED;
    }
}
