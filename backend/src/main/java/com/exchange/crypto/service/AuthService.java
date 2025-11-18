package com.exchange.crypto.service;

import com.exchange.crypto.dto.AuthResponse;
import com.exchange.crypto.dto.LoginRequest;
import com.exchange.crypto.dto.RegisterRequest;
import com.exchange.crypto.model.User;
import com.exchange.crypto.repository.UserRepository;
import com.exchange.crypto.security.JwtUtils;
import com.exchange.crypto.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(item -> item.getAuthority())
                .orElse("ROLE_USER");

        return new AuthResponse(jwt, userDetails.getId(), userDetails.getUsername(),
                userDetails.getEmail(), role);
    }

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setRole(User.UserRole.USER);
        user.setEnabled(true);

        userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(registerRequest.getUsername(), registerRequest.getPassword())
        );

        String jwt = jwtUtils.generateJwtToken(authentication);

        return new AuthResponse(jwt, user.getId(), user.getUsername(),
                user.getEmail(), "ROLE_" + user.getRole().name());
    }
}
