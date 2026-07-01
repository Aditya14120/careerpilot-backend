package com.careerpilot.careerpilot.skill.repository;

import com.careerpilot.careerpilot.skill.entity.ExtractedSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExtractedSkillRepository extends JpaRepository<ExtractedSkill, Long> {

    List<ExtractedSkill> findByResumeIdOrderByNameAsc(Long resumeId);

    void deleteByResumeId(Long resumeId);
}
