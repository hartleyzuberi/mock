package com.aceli.mock.repository;

import com.aceli.mock.domain.FundingRequest;
import com.aceli.mock.domain.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FundingRequestRepository extends JpaRepository<FundingRequest, Long> {
    List<FundingRequest> findAllByStatusOrderByCreatedAtDesc(RequestStatus status);
    List<FundingRequest> findAllByOrderByCreatedAtDesc();
}
