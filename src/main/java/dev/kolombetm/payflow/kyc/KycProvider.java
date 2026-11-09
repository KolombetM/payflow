package dev.kolombetm.payflow.kyc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!dev & !test")
public class KycProvider implements KycProviderInterface {

    public KycProvider(@Value("${payflow.kyc.api-key:}") String apiKey) {
        if  (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("payflow.kyc.api-key is required");
        }
    }

    @Override
    public KycStatus verify(String userId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
