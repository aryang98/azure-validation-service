package com.example.forecasting.repository;

import com.example.forecasting.model.EstimationResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstimationResultRepository extends JpaRepository<EstimationResult, Long> {
    Optional<EstimationResult> findByUserRequestId(Long userRequestId);
} 