package com.careerpilot.careerpilot.skill.repository;

import com.careerpilot.careerpilot.skill.entity.ExtractedSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ExtractedSkillRepository extends JpaRepository<ExtractedSkill, Long> {

    List<ExtractedSkill> findByResumeIdOrderByNameAsc(Long resumeId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ExtractedSkill e WHERE e.resume.id = :resumeId")
    void deleteByResumeId(@Param("resumeId") Long resumeId);
}
