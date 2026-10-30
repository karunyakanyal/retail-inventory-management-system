package com.hcl.rims.service;

import com.hcl.rims.dto.UserDTO;
import com.hcl.rims.exception.BadRequestException;
import com.hcl.rims.exception.ResourceNotFoundException;
import com.hcl.rims.model.Role;
import com.hcl.rims.model.User;
import com.hcl.rims.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    public List<UserDTO> findAll() {
        return userRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public UserDTO findById(Long id) {
        return userRepository.findById(id)
            .map(this::toDTO)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    @Transactional
    public UserDTO createUser(UserDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BadRequestException("Username '" + dto.getUsername() + "' is already taken");
        }
        if (dto.getPassword() == null || dto.getPassword().trim().length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters long");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setEnabled(dto.isEnabled());

        User saved = userRepository.save(user);
        auditService.log("CREATE_USER", "User", saved.getId(), "Created user: " + saved.getUsername() + " with role " + saved.getRole());
        return toDTO(saved);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserDTO dto) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        if (!user.getUsername().equalsIgnoreCase(dto.getUsername()) && userRepository.existsByUsername(dto.getUsername())) {
            throw new BadRequestException("Username '" + dto.getUsername() + "' is already taken");
        }

        user.setUsername(dto.getUsername());
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            if (dto.getPassword().trim().length() < 6) {
                throw new BadRequestException("Password must be at least 6 characters long");
            }
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }
        user.setEnabled(dto.isEnabled());

        User saved = userRepository.save(user);
        auditService.log("UPDATE_USER", "User", saved.getId(), "Updated user: " + saved.getUsername());
        return toDTO(saved);
    }

    @Transactional
    public UserDTO deactivateUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        user.setEnabled(false);
        User saved = userRepository.save(user);
        auditService.log("DEACTIVATE_USER", "User", saved.getId(), "Deactivated user: " + saved.getUsername());
        return toDTO(saved);
    }

    @Transactional
    public UserDTO activateUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        user.setEnabled(true);
        User saved = userRepository.save(user);
        auditService.log("ACTIVATE_USER", "User", saved.getId(), "Activated user: " + saved.getUsername());
        return toDTO(saved);
    }

    @Transactional
    public UserDTO assignRole(Long id, Role role) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        user.setRole(role);
        User saved = userRepository.save(user);
        auditService.log("ASSIGN_ROLE", "User", saved.getId(), "Assigned role " + role + " to user: " + saved.getUsername());
        return toDTO(saved);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        userRepository.delete(user);
        auditService.log("DELETE_USER", "User", id, "Deleted user: " + user.getUsername());
    }

    public UserDTO toDTO(User user) {
        return new UserDTO(
            user.getId(),
            user.getUsername(),
            user.getFullName(),
            user.getEmail(),
            user.getRole(),
            user.isEnabled()
        );
    }
}
