package com.careerpilot.careerpilot.career.entity;

import com.careerpilot.careerpilot.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "gap_analyses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GapAnalysis {

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

    private Integer overallMatch;

    @Column(columnDefinition = "TEXT")
    private String resultJson;

    private LocalDateTime analyzedAt;

    @PrePersist
    public void prePersist() {
        analyzedAt = LocalDateTime.now();
    }
}
