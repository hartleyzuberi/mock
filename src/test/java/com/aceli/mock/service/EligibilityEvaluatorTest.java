package com.aceli.mock.service;

import com.aceli.mock.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class EligibilityEvaluatorTest {
    private final EligibilityEvaluator evaluator = new EligibilityEvaluator();
    private final CountryLimit kenya = new CountryLimit(Country.KENYA, new BigDecimal("100000.00"));

    @Test
    void approvesAmountBelowLimitForEstablishedOrganization() {
        var decision = evaluator.evaluate(request("99999.99", 3), kenya);
        assertThat(decision.status()).isEqualTo(RequestStatus.APPROVED);
        assertThat(decision.reasons()).isEmpty();
    }

    @Test
    void approvesAmountExactlyAtLimit() {
        var decision = evaluator.evaluate(request("100000.00", 2), kenya);
        assertThat(decision.status()).isEqualTo(RequestStatus.APPROVED);
    }

    @Test
    void rejectsAmountAboveLimit() {
        var decision = evaluator.evaluate(request("100000.01", 3), kenya);
        assertThat(decision.status()).isEqualTo(RequestStatus.REJECTED);
        assertThat(decision.reasons()).containsExactly(RejectionReason.AMOUNT_EXCEEDS_COUNTRY_LIMIT);
    }

    @Test
    void rejectsOrganizationYoungerThanTwoYears() {
        var decision = evaluator.evaluate(request("50000.00", 1), kenya);
        assertThat(decision.status()).isEqualTo(RequestStatus.REJECTED);
        assertThat(decision.reasons()).containsExactly(RejectionReason.ORGANIZATION_TOO_YOUNG);
    }

    @Test
    void approvesOrganizationExactlyTwoYearsOld() {
        var decision = evaluator.evaluate(request("50000.00", 2), kenya);
        assertThat(decision.status()).isEqualTo(RequestStatus.APPROVED);
    }

    @Test
    void returnsAllApplicableRejectionReasons() {
        var decision = evaluator.evaluate(request("100000.01", 1), kenya);
        assertThat(decision.reasons()).containsExactly(
                RejectionReason.ORGANIZATION_TOO_YOUNG,
                RejectionReason.AMOUNT_EXCEEDS_COUNTRY_LIMIT
        );
    }

    private FundingRequest request(String amount, int age) {
        return FundingRequest.create("Test Organization", Country.KENYA, new BigDecimal(amount), age);
    }
}
