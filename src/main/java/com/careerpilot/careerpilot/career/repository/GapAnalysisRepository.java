package com.careerpilot.careerpilot.career.repository;

import com.careerpilot.careerpilot.career.entity.GapAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GapAnalysisRepository extends JpaRepository<GapAnalysis, Long> {

    List<GapAnalysis> findByUser_EmailOrderByAnalyzedAtDesc(String email);

    Optional<GapAnalysis> findFirstByUser_EmailAndTargetRoleIgnoreCaseOrderByAnalyzedAtDesc(
            String email, String targetRole);
}
