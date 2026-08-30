package com.aceli.mock.service;

import com.aceli.mock.domain.CountryLimit;
import com.aceli.mock.domain.FundingRequest;
import com.aceli.mock.domain.RequestStatus;
import com.aceli.mock.exception.FundingRequestException;
import com.aceli.mock.repository.CountryLimitRepository;
import com.aceli.mock.repository.FundingRequestRepository;
import com.aceli.mock.web.FundingRequestDtos.CreateFundingRequestRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class FundingRequestService {
    private final FundingRequestRepository requestRepository;
    private final CountryLimitRepository limitRepository;
    private final EligibilityEvaluator evaluator;

    public FundingRequestService(FundingRequestRepository requestRepository,
                                 CountryLimitRepository limitRepository,
                                 EligibilityEvaluator evaluator) {
        this.requestRepository = requestRepository;
        this.limitRepository = limitRepository;
        this.evaluator = evaluator;
    }

    @Transactional
    public FundingRequest create(CreateFundingRequestRequest input) {
        FundingRequest request = FundingRequest.create(
                input.organizationName(),
                input.country(),
                input.requestedAmount(),
                input.organizationAgeYears()
        );
        return requestRepository.save(request);
    }

    @Transactional
    public EligibilityEvaluator.EvaluationDecision evaluate(Long id) {
        FundingRequest request = getRequired(id);
        CountryLimit limit = limitRepository.findById(request.getCountry())
                .orElseThrow(() -> new FundingRequestException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "COUNTRY_LIMIT_NOT_CONFIGURED",
                        "No eligibility limit is configured for " + request.getCountry().displayName()
                ));

        EligibilityEvaluator.EvaluationDecision decision = evaluator.evaluate(request, limit);
        request.applyDecision(decision.status(), decision.reasons());
        requestRepository.save(request);
        return decision;
    }

    public FundingRequest get(Long id) {
        return getRequired(id);
    }

    public List<FundingRequest> list(RequestStatus status) {
        return status == null
                ? requestRepository.findAllByOrderByCreatedAtDesc()
                : requestRepository.findAllByStatusOrderByCreatedAtDesc(status);
    }

    private FundingRequest getRequired(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new FundingRequestException(
                        HttpStatus.NOT_FOUND,
                        "FUNDING_REQUEST_NOT_FOUND",
                        "Funding request " + id + " was not found"
                ));
    }
}
