package org.project.doodle.controller;

import jakarta.validation.Valid;
import org.project.doodle.controller.dto.CreateUserRequest;
import org.project.doodle.controller.dto.UserResponse;
import org.project.doodle.domain.User;
import org.project.doodle.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest req) {
        User user = userService.create(req.name(), req.email());
        return UserResponse.from(user);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> get(@PathVariable Long userId) {
        return ResponseEntity.ok(UserResponse.from(userService.get(userId)));
    }
}
