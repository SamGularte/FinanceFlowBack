package com.samuelgularte.financeflow.auth.infrastructure.security;

import com.samuelgularte.financeflow.auth.infrastructure.persistance.entity.User;
import com.samuelgularte.financeflow.auth.infrastructure.persistance.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(username).orElseThrow(() -> new RuntimeException("User not found: " + username));
        return new CustomUserDetails(user);
    }
}
