package org.project.doodle.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.doodle.domain.User;
import org.project.doodle.exception.ConflictException;
import org.project.doodle.exception.NotFoundException;
import org.project.doodle.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    @Test
    void createPersistsNewUser() {
        when(userRepository.existsByEmail("adam@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User created = userService.create("Adam Smith", "adam@example.com");

        assertThat(created.getEmail()).isEqualTo("adam@example.com");
        assertThat(created.getCalendar()).isNotNull();
    }

    @Test
    void createWithDuplicateEmailThrowsConflict() {
        when(userRepository.existsByEmail("adam@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create("Adam Smith", "adam@example.com"))
                .isInstanceOf(ConflictException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void getMissingUserThrowsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.get(99L))
                .isInstanceOf(NotFoundException.class);
    }
}
