package com.careerpilot.careerpilot.interview.repository;

import com.careerpilot.careerpilot.interview.entity.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {

    List<InterviewSession> findByUser_EmailOrderByGeneratedAtDesc(String email);
}
