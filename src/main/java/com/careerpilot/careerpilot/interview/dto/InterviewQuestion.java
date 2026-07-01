package com.careerpilot.careerpilot.interview.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InterviewQuestion {

    private String question;
    private String type;        // Technical, Behavioral, Situational
    private String difficulty;  // Easy, Medium, Hard
    private String hint;
}
