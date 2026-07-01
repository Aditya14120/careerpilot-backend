package com.careerpilot.careerpilot.resume.repository;

import com.careerpilot.careerpilot.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByUserIdOrderByUploadedAtDesc(Long userId);
}
