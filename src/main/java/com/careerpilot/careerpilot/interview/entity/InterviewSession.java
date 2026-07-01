package com.careerpilot.careerpilot.interview.entity;

import com.careerpilot.careerpilot.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interview_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Long resumeId;

    @Column(nullable = false)
    private String targetRole;

    @Column(nullable = false)
    private Integer totalQuestions;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String resultJson;

    private LocalDateTime generatedAt;

    @PrePersist
    public void prePersist() {
        generatedAt = LocalDateTime.now();
    }
}
