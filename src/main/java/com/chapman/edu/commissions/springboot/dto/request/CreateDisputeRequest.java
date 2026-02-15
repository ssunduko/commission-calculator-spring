package com.chapman.edu.commissions.springboot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new Dispute.
 *
 * CONCEPT: Custom Validation Messages
 * --------------------------------------
 * Each validation annotation accepts a "message" parameter that defines the
 * error message shown when validation fails. This message is included in the
 * error response sent to the client.
 *
 * You can also externalize messages to a properties file:
 *   @NotBlank(message = "{dispute.title.required}")
 *   (Defined in ValidationMessages.properties)
 */
public class CreateDisputeRequest {

    @NotBlank(message = "Calculation ID is required")
    private String calculationId;

    @NotBlank(message = "Sales representative ID is required")
    private String salesRepId;

    @NotBlank(message = "Dispute title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;

    @NotBlank(message = "Dispute description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;

    // --- Getters and Setters ---

    public String getCalculationId() {
        return calculationId;
    }

    public void setCalculationId(String calculationId) {
        this.calculationId = calculationId;
    }

    public String getSalesRepId() {
        return salesRepId;
    }

    public void setSalesRepId(String salesRepId) {
        this.salesRepId = salesRepId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
