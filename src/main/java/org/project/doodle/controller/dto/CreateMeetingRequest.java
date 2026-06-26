package org.project.doodle.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateMeetingRequest(

        @NotBlank @Size(max = 255) String title,

        @Size(max = 2000) String description,

        @Size(max = 100)
        Set<@Email
            @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                    message = "each participant must be a valid email address")
            String> participants) {
}
