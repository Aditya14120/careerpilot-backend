package com.careerpilot.careerpilot.skill.entity;

import com.careerpilot.careerpilot.resume.entity.Resume;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "extracted_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtractedSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    private String proficiencyLevel;

    private LocalDateTime extractedAt;
}
