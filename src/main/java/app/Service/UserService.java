package app.Service;

import app.DTO.User.UpdateUserRequest;
import app.DTO.User.UserDTO;
import app.Database.User;
import app.DTO.User.UserMapper;
import app.Repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    public Boolean registerUser(User user) {
        if(userRepository.existsByEmail(user.getEmail())) {
            return Boolean.FALSE;
        }

        userRepository.save(user);
        return true;
    }

    public User getUser(String email) {
        if(userRepository.existsByEmail(email)) {
            return userRepository.findByEmail(email).get();
        }

        return null;
    }

    public UserDTO getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        return userMapper.toDTO(user);
    }

    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        return userMapper.toDTO(user);
    }


    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    public UserDTO updateUser(UUID userId, @Valid UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Update fields if provided
        if (request.getName() != null && !request.getName().isEmpty()) {
            user.setName(request.getName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getProfilePhotoUrl() != null) {
            user.setProfilePhotoUrl(request.getProfilePhotoUrl());
        }

        // Save updated user
        User updatedUser = userRepository.save(user);

        // Return DTO
        return userMapper.toDTO(updatedUser);
    }

    /**
     * Delete a user
     */
    @Transactional
    public void deleteUser(UUID userId) {
        // Check if user exists
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }

        // Delete the user
        userRepository.deleteById(userId);
    }

    /**
     * Check if user exists by email
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
