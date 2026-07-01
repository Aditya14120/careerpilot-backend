package com.careerpilot.careerpilot.interview.service;

import com.careerpilot.careerpilot.interview.dto.InterviewRequest;
import com.careerpilot.careerpilot.interview.dto.InterviewResponse;

import java.util.List;

public interface InterviewService {

    InterviewResponse generate(InterviewRequest request, String userEmail);

    List<InterviewResponse> getMySessions(String userEmail);

    InterviewResponse getSession(Long sessionId, String userEmail);
}
