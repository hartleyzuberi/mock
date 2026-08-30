package com.aceli.mock.web;

import com.aceli.mock.domain.Country;
import com.aceli.mock.domain.FundingRequest;
import com.aceli.mock.domain.RejectionReason;
import com.aceli.mock.domain.RequestStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public final class FundingRequestDtos {
    private FundingRequestDtos() {}

    public record CreateFundingRequestRequest(
            @NotBlank @Size(max = 200) String organizationName,
            @NotNull Country country,
            @NotNull @DecimalMin(value = "0.00", inclusive = false) @Digits(integer = 13, fraction = 2) BigDecimal requestedAmount,
            @NotNull @Min(0) Integer organizationAgeYears
    ) {}

    public record FundingRequestResponse(
            Long id,
            String organizationName,
            Country country,
            BigDecimal requestedAmount,
            int organizationAgeYears,
            RequestStatus status,
            List<String> decisionReasons,
            Instant createdAt
    ) {
        public static FundingRequestResponse from(FundingRequest request) {
            List<String> reasons = request.getDecisionReason() == null || request.getDecisionReason().isBlank()
                    ? List.of()
                    : Arrays.asList(request.getDecisionReason().split(","));
            return new FundingRequestResponse(
                    request.getId(), request.getOrganizationName(), request.getCountry(),
                    request.getRequestedAmount(), request.getOrganizationAgeYears(), request.getStatus(),
                    reasons, request.getCreatedAt()
            );
        }
    }

    public record EvaluationResponse(Long requestId, RequestStatus status, List<RejectionReason> reasons) {}
}
