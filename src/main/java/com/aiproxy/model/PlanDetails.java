package com.aiproxy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response model for subscription plan details.
 * Contains plan information and associated limits.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanDetails {
    
    private String plan;
    
    private int requestsPerMinute;
    
    private int monthlyTokens;
}
