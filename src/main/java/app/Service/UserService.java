package app.Service;

import app.DTO.User.UpdateUserRequest;
import app.DTO.User.UserDTO;
import app.DTO.User.UserMapper;
import app.Database.DatabaseType;
import app.Database.User;
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

    @Autowired
    private GlobalShortCodeService globalShortCodeService;

    public Boolean registerUser(User user) {
        if(userRepository.existsByEmail(user.getEmail())) {
            return Boolean.FALSE;
        }

        userRepository.save(user);

        UUID userUUID = user.getId();
        globalShortCodeService.generateAndReserve(DatabaseType.USER, userUUID);

        return true;
    }

    public User getUser(String email) {
        if(userRepository.existsByEmail(email)) {
            return userRepository.findByEmail(email).get();
        }

        return null;
    }

    public User getUserByUUID(UUID uuid) {
        return userRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("User does not exist"));
    }

    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        return userMapper.toDTO(user);
    }

    public String getUserShortCodeByEmail(String email) {
        UserDTO userDTO = getUserByEmail(email);
        return globalShortCodeService.getShortCode(DatabaseType.USER, userDTO.getId());
    }

    public User getUserByShortCode(String code) {
        UUID userUUID = globalShortCodeService.getUUIDfromShortCode(
                DatabaseType.USER,
                code
        );

        return getUserByUUID(userUUID);
    }

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    public UserDTO updateUser(String shortCode, @Valid UpdateUserRequest request) {
        User user = getUserByShortCode(shortCode);

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
        User user = getUserByShortCode(shortCode);

        // Delete the user
        userRepository.deleteById(user.getId());
    }

    /**
     * Check if user exists by email
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public String getShortCodeByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User does not exist"));

        return globalShortCodeService.getShortCode(DatabaseType.USER, user.getId());
    }
}
