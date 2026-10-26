package org.example.resumeapp.SecurityConfig;

import lombok.RequiredArgsConstructor;
import org.example.resumeapp.Entity.UserEntity;
import org.example.resumeapp.Repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User.withUsername(user.getEmail())
                .password(user.getPassword())
                .roles("USER")
                .disabled(!user.isEmailVerified())
                .build();
    }
}