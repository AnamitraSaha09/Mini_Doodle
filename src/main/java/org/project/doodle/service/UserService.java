package org.project.doodle.service;

import org.project.doodle.domain.User;
import org.project.doodle.exception.ConflictException;
import org.project.doodle.exception.NotFoundException;
import org.project.doodle.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User create(String name, String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("A user with email '" + email + "' already exists");
        }
        // Also creates the calendar entry
        return userRepository.save(new User(name, email));
    }

    @Transactional(readOnly = true)
    public User get(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User " + id + " not found"));
    }
}
