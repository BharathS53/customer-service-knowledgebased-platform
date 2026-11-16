package com.example.customerservice.security;

import com.example.customerservice.entity.User;
import com.example.customerservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        if (usernameOrEmail == null || usernameOrEmail.trim().isBlank()) {
            throw new UsernameNotFoundException("Username or email cannot be empty");
        }

        String identifier = usernameOrEmail.trim();

        // Support friendly role-based aliases: 'customer' -> 'john_doe', 'agent' -> 'agent_sarah'
        if ("customer".equalsIgnoreCase(identifier)) {
            identifier = "john_doe";
        } else if ("agent".equalsIgnoreCase(identifier)) {
            identifier = "agent_sarah";
        }

        final String lookupKey = identifier;
        User user = userRepository.findByUsername(lookupKey)
                .or(() -> userRepository.findByEmail(lookupKey.toLowerCase()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));

        return new CustomUserDetails(user);
    }
}
