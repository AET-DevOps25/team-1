package de.tum.devops.application.dto;

import de.tum.devops.application.persistence.enums.DecisionEnum;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating applications (HR decision) according to
 * api-documentation.yaml
 * 
 * Schema definition:
 * UpdateApplicationRequest:
 * properties:
 * hrDecision: string (enum: [SHORTLISTED, REJECTED, HIRED]) (required)
 * hrComments: string (optional)
 */
public class UpdateApplicationRequest {

    @NotNull(message = "HR decision is required")
    private DecisionEnum hrDecision;

    private String hrComments;

    // Constructors
    public UpdateApplicationRequest() {
    }

    public UpdateApplicationRequest(DecisionEnum hrDecision, String hrComments) {
        this.hrDecision = hrDecision;
        this.hrComments = hrComments;
    }

    // Getters and Setters
    public DecisionEnum getHrDecision() {
        return hrDecision;
    }

    public void setHrDecision(DecisionEnum hrDecision) {
        this.hrDecision = hrDecision;
    }

    public String getHrComments() {
        return hrComments;
    }

    public void setHrComments(String hrComments) {
        this.hrComments = hrComments;
    }
}