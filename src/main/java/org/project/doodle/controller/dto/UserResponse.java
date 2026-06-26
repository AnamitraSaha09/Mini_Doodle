package org.project.doodle.controller.dto;

import org.project.doodle.domain.User;

public record UserResponse(Long id, String name, String email) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail());
    }
}
