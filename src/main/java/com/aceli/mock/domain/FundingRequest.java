package com.aceli.mock.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "funding_requests", indexes = {
        @Index(name = "idx_funding_requests_status", columnList = "status")
})
public class FundingRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_name", nullable = false, length = 200)
    private String organizationName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Country country;

    @Column(name = "requested_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "organization_age_years", nullable = false)
    private int organizationAgeYears;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status;

    @Column(name = "decision_reason", length = 500)
    private String decisionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected FundingRequest() {
    }

    private FundingRequest(String organizationName, Country country, BigDecimal requestedAmount, int organizationAgeYears) {
        this.organizationName = organizationName.trim();
        this.country = country;
        this.requestedAmount = requestedAmount;
        this.organizationAgeYears = organizationAgeYears;
        this.status = RequestStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public static FundingRequest create(String organizationName, Country country, BigDecimal requestedAmount, int organizationAgeYears) {
        return new FundingRequest(organizationName, country, requestedAmount, organizationAgeYears);
    }

    public void applyDecision(RequestStatus newStatus, List<RejectionReason> reasons) {
        if (newStatus == RequestStatus.PENDING) {
            throw new IllegalArgumentException("Evaluation cannot return PENDING");
        }
        this.status = newStatus;
        this.decisionReason = reasons == null || reasons.isEmpty()
                ? null
                : String.join(",", reasons.stream().map(Enum::name).toList());
    }

    public Long getId() { return id; }
    public String getOrganizationName() { return organizationName; }
    public Country getCountry() { return country; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public int getOrganizationAgeYears() { return organizationAgeYears; }
    public RequestStatus getStatus() { return status; }
    public String getDecisionReason() { return decisionReason; }
    public Instant getCreatedAt() { return createdAt; }
}
