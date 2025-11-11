package com.example.moki_campaign.domain.customer.entity;

public enum CustomerSegment {
    
    GENERAL, LOYAL, CHURN_RISK, AT_RISK_LOYAL;

    public static CustomerSegment fromString(String segmentString) {
        if (segmentString == null || segmentString.trim().isEmpty()) {
            return GENERAL;
        }

        String normalized = segmentString.trim().toUpperCase();

        try {
            return CustomerSegment.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return GENERAL;
        }
    }
}
