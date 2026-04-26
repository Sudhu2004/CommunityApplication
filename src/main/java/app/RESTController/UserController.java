package app.RESTController;

import app.DTO.User.UpdateUserRequest;
import app.DTO.User.UserDTO;
import app.Database.User;
import app.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * GET /api/user/{userCode}
     * Get a user by ID
     */
    @GetMapping("/{userCode}")
    public ResponseEntity<UserDTO> getUserbyCode(@PathVariable String userCode) {
        UserDTO user = userService.getUserByShortId(userCode);

        return ResponseEntity.ok(user);
    }

    /**
     * GET /api/user/email/{email}
     * Get a user by email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        UserDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    /**
     * GET /api/user/all
     * Get all users
     */
    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * PUT /api/user/{userCode}
     * Update an existing user
     */
    @PutMapping("/{userCode}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable String userCode,
            @Valid @RequestBody UpdateUserRequest request) {
        UserDTO updatedUser = userService.updateUser(userCode, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * DELETE /api/user/{userCode}
     * Delete a user
     */
    @DeleteMapping("/{userCode}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userCode) {
        userService.deleteUser(userCode);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/user/exists/{email}
     * Check if a user exists by email
     */
    @GetMapping("/exists/{email}")
    public ResponseEntity<Boolean> checkEmailExists(@PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    /**
     * GET /api/user/userCodeByEmail/{email}
     * Check if a user exists by email
     */

    @GetMapping("/userCodeByEmail")
    public ResponseEntity<String> getShortCodeByEmail(
            @RequestParam String email
    ) {

        UserDTO user = userService.getUserByEmail(email);

        return ResponseEntity.ok(user.getUserCode());
    }
}
