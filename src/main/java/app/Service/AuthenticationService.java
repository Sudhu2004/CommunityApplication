package app.Service;

import app.Config.JwtService;
import app.DTO.Auth.AuthRequest;
import app.DTO.Auth.AuthResponse;
import app.DTO.Auth.SignUpRequest;
import app.Database.User;
import app.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthResponse register(SignUpRequest request) {
        if(request.getEmail() != null) {
            boolean existsByEmail = userRepository.existsByEmail(request.getEmail());
            if(existsByEmail) {
                throw new RuntimeException("User Email: " + request.getEmail() + " exists");
            }
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        if(request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        userRepository.save(user);
        String jwtToken = jwtService.generateToken(user.getEmail());

        return new AuthResponse(
                jwtToken,
                user.getEmail()
        );
    }

    public AuthResponse authenticate(AuthRequest authRequest) {
        String email = authRequest.getEmail();
        String password = authRequest.getPassword();

        try {
            System.out.println("Authentication manager");
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            password
                    )
            );
            System.out.println("Authentication manager Completed");

            String jwtToken = jwtService.generateToken(email);
            System.out.println("Sending Token: " + jwtToken);
            return new AuthResponse(jwtToken, email);
        } catch (Exception e) {
            System.err.println("Authentication failed: " + e.getMessage());
            return new AuthResponse(null, null);  // Return failed response
        }
    }
}
