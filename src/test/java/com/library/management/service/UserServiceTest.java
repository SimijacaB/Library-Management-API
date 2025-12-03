package com.library.management.service;

import com.library.management.model.User;
import com.library.management.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void whenFindAll_thenReturnUserList() {
        // given
        User user = new User();
        user.setName("Test User");
        user.setRole("USER");

        List<User> userList = Collections.singletonList(user);
        when(userRepository.findAll()).thenReturn(userList);

        // when
        List<User> foundUsers = userService.findAll();

        // then
        assertThat(foundUsers).isNotEmpty();
        assertThat(foundUsers.get(0).getName()).isEqualTo("Test User");
    }

    @Test
    public void whenFindById_thenReturnUser() {
        // given
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setRole("USER");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        Optional<User> foundUser = userService.findById(1L);

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Test User");
    }
}
