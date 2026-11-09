package dev.kolombetm.payflow.kyc;

public interface KycProviderInterface {
    KycStatus verify(String userId);
}
