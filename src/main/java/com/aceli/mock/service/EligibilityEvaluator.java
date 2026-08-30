package com.aceli.mock.service;

import com.aceli.mock.domain.CountryLimit;
import com.aceli.mock.domain.FundingRequest;
import com.aceli.mock.domain.RejectionReason;
import com.aceli.mock.domain.RequestStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EligibilityEvaluator {
    public EvaluationDecision evaluate(FundingRequest request, CountryLimit countryLimit) {
        List<RejectionReason> reasons = new ArrayList<>();

        if (request.getOrganizationAgeYears() < 2) {
            reasons.add(RejectionReason.ORGANIZATION_TOO_YOUNG);
        }
        if (request.getRequestedAmount().compareTo(countryLimit.getMaxAmount()) > 0) {
            reasons.add(RejectionReason.AMOUNT_EXCEEDS_COUNTRY_LIMIT);
        }

        return reasons.isEmpty()
                ? new EvaluationDecision(RequestStatus.APPROVED, List.of())
                : new EvaluationDecision(RequestStatus.REJECTED, List.copyOf(reasons));
    }

    public record EvaluationDecision(RequestStatus status, List<RejectionReason> reasons) {
    }
}
