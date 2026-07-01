package com.careerpilot.careerpilot.career.service;

import com.careerpilot.careerpilot.career.dto.GapAnalysisRequest;
import com.careerpilot.careerpilot.career.dto.GapAnalysisResponse;

import java.util.List;

public interface GapAnalysisService {

    GapAnalysisResponse analyze(GapAnalysisRequest request, String userEmail);

    List<GapAnalysisResponse> getMyAnalyses(String userEmail);
}
