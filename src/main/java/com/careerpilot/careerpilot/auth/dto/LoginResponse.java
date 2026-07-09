package com.careerpilot.careerpilot.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String message;

    // User profile fields — returned on login so frontend doesn't need a separate /me call
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String college;
    private String branch;
    private Integer graduationYear;
    private String role;
}
