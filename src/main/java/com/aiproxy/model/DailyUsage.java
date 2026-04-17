package com.aiproxy.model;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response model for daily usage statistics.
 * Represents token consumption for a specific date.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyUsage {
    
    private LocalDate date;
    
    private int tokensUsed;
}
