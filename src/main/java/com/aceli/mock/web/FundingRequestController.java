package com.aceli.mock.web;

import com.aceli.mock.domain.RequestStatus;
import com.aceli.mock.service.EligibilityEvaluator;
import com.aceli.mock.service.FundingRequestService;
import com.aceli.mock.web.FundingRequestDtos.CreateFundingRequestRequest;
import com.aceli.mock.web.FundingRequestDtos.EvaluationResponse;
import com.aceli.mock.web.FundingRequestDtos.FundingRequestResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/funding-requests")
public class FundingRequestController {
    private final FundingRequestService service;

    public FundingRequestController(FundingRequestService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FundingRequestResponse create(@Valid @RequestBody CreateFundingRequestRequest request) {
        return FundingRequestResponse.from(service.create(request));
    }

    @PostMapping("/{id}/evaluate")
    public EvaluationResponse evaluate(@PathVariable Long id) {
        EligibilityEvaluator.EvaluationDecision decision = service.evaluate(id);
        return new EvaluationResponse(id, decision.status(), decision.reasons());
    }

    @GetMapping("/{id}")
    public FundingRequestResponse get(@PathVariable Long id) {
        return FundingRequestResponse.from(service.get(id));
    }

    @GetMapping
    public List<FundingRequestResponse> list(@RequestParam(required = false) RequestStatus status) {
        return service.list(status).stream().map(FundingRequestResponse::from).toList();
    }
}
