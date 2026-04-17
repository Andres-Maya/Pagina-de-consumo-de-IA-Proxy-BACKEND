package com.aiproxy.model;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response model for quota status information.
 * Provides current usage and remaining quota details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuotaStatus {
    
    private int tokensUsed;
    
    private int tokensRemaining;
    
    private LocalDate resetDate;
    
    private String currentPlan;
}
