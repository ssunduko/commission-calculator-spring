package com.chapman.edu.commissions.ai.agent;

import com.chapman.edu.commissions.ai.service.agent.Tool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the calculate_total tool's executor logic.
 * Extracted to verify the arithmetic operations independently.
 */
@DisplayName("calculate_total Tool — Unit Tests")
class CalculateToolTest {

    private Tool calculateTool;

    @BeforeEach
    void setUp() {
        // Recreate the tool executor logic from CommissionToolRegistry
        calculateTool = new Tool("calculate_total", "Calculate commission", input -> {
            String trimmed = input.trim();

            if (trimmed.toLowerCase().startsWith("sum:")) {
                String[] values = trimmed.substring(4).split(",");
                java.math.BigDecimal total = java.math.BigDecimal.ZERO;
                for (String val : values) {
                    try {
                        total = total.add(new java.math.BigDecimal(val.trim()));
                    } catch (NumberFormatException e) {
                        return "Error: Invalid number '" + val.trim() + "' in sum.";
                    }
                }
                return String.format("Sum total: $%s",
                        total.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
            }

            if (trimmed.contains("*")) {
                String[] parts = trimmed.split("\\*");
                if (parts.length != 2) {
                    return "Error: Expected format 'value * rate_percent' (e.g., '150000 * 12').";
                }
                try {
                    java.math.BigDecimal value = new java.math.BigDecimal(parts[0].trim());
                    java.math.BigDecimal rate = new java.math.BigDecimal(parts[1].trim());
                    java.math.BigDecimal commission = value.multiply(rate)
                            .divide(java.math.BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                    return String.format("$%s * %s%% = $%s",
                            value.toPlainString(), rate.toPlainString(), commission.toPlainString());
                } catch (NumberFormatException e) {
                    return "Error: Could not parse numbers. Use format: '150000 * 12'.";
                }
            }

            return "Error: Unknown format. Use 'value * rate_percent' or 'sum:v1,v2,v3'.";
        });
    }

    @Test
    @DisplayName("should calculate percentage correctly")
    void shouldCalculatePercentage() {
        String result = calculateTool.execute("150000 * 12");
        assertThat(result).contains("$18000.00");
    }

    @Test
    @DisplayName("should calculate small percentage correctly")
    void shouldCalculateSmallPercentage() {
        String result = calculateTool.execute("35000 * 5");
        assertThat(result).contains("$1750.00");
    }

    @Test
    @DisplayName("should sum values correctly")
    void shouldSumValues() {
        String result = calculateTool.execute("sum:1000,2000,3000");
        assertThat(result).contains("$6000.00");
    }

    @Test
    @DisplayName("should handle single sum value")
    void shouldHandleSingleSumValue() {
        String result = calculateTool.execute("sum:5000");
        assertThat(result).contains("$5000.00");
    }

    @Test
    @DisplayName("should return error for invalid multiplication format")
    void shouldErrorOnInvalidMultiplication() {
        String result = calculateTool.execute("abc * xyz");
        assertThat(result).contains("Error");
    }

    @Test
    @DisplayName("should return error for invalid sum value")
    void shouldErrorOnInvalidSumValue() {
        String result = calculateTool.execute("sum:100,abc,200");
        assertThat(result).contains("Error").contains("abc");
    }

    @Test
    @DisplayName("should return error for unknown format")
    void shouldErrorOnUnknownFormat() {
        String result = calculateTool.execute("just some text");
        assertThat(result).contains("Error").contains("Unknown format");
    }

    @Test
    @DisplayName("should handle decimal rates")
    void shouldHandleDecimalRates() {
        String result = calculateTool.execute("100000 * 7.5");
        assertThat(result).contains("$7500.00");
    }
}
