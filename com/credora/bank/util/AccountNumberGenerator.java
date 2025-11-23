package com.credora.bank.util;

public class AccountNumberGenerator {

    // Simple: CRD + 4-digit customerId. For real app, use a proper sequence.
    public static String generate(long customerId) {
        return String.format("CRD%04d", customerId);
    }
}
