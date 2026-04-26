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

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    private final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private final int DEFAULT_LENGTH = 8;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        return generate(DEFAULT_LENGTH);
    }

    private String generate(int length) {
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(BASE62.length());
            sb.append(BASE62.charAt(index));
        }

        return sb.toString();
    }

    public Boolean registerUser(User user) {
        if(userRepository.existsByEmail(user.getEmail())) {
            return Boolean.FALSE;
        }

        String code;
        do {
            code = generate();
        } while (userRepository.existsByUserCode(code));

        user.setUserCode(code);

        userRepository.save(user);
        return true;
    }

    public User getUser(String email) {
        if(userRepository.existsByEmail(email)) {
            return userRepository.findByEmail(email).get();
        }

        return null;
    }

    public UserDTO getUserByShortId(String shortCode) {
        User user = userRepository.findByUserCode(shortCode)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + shortCode));

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

    public UserDTO updateUser(String shortCode, @Valid UpdateUserRequest request) {
        User user = userRepository.findByUserCode(shortCode)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + shortCode));

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
    public void deleteUser(String shortCode) {
        // Check if user exists
        if (!userRepository.existsByUserCode(shortCode)) {
            throw new RuntimeException("User not found with id: " + shortCode);
        }

        // get uuid
        User user = userRepository.findByUserCode(shortCode)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + shortCode));

        // Delete the user
        userRepository.deleteById(user.getId());
    }

    /**
     * Check if user exists by email
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

}
