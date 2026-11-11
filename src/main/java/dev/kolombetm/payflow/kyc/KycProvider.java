package dev.kolombetm.payflow.kyc;

public interface KycProvider {
    KycStatus verify(String userId);
}
