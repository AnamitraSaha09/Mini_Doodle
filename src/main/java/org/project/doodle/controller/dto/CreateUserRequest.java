package org.project.doodle.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank
        @Size(max = 255)
        @Pattern(regexp = "^[\\p{L} .'-]+$",
                message = "Name may contain only letters, spaces, and . ' -")
        String name,

        @NotBlank
        @Size(max = 320)
        @Email(message = "Must be a valid email address")
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                message = "Must be a valid email address")
        String email
) {
}
